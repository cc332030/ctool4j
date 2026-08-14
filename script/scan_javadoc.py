# -*- coding: utf-8 -*-
"""
扫描项目所有 Java 文件，找出缺少 javadoc 的 public/protected 声明。

规则（对齐 AGENTS-JAVA.MD）：
- 所有 public / protected 的类、方法、字段必须编写 javadoc
- @Override 方法除外（继承父类文档，doclint 不检查）
- 枚举常量、注解元素视为字段
"""
import os
import re
import sys

ROOT = r"d:\file\gzg\changsha\cc332030\ctool4j-2\ctool4j-parent"

# 声明正则（忽略前导修饰符，只识别类型关键字 + 名称）
CLASS_RE = re.compile(r'^\s*(?:public|protected)\s+(?:abstract\s+|final\s+|strictfp\s+)*(?:class|interface|enum|@interface)\s+(\w+)')
# 方法/字段类型正则（去掉修饰符后，形如 "Type name(...)" 或 "Type name = ..." / "Type name;"）
DECL_RE = re.compile(r'^\s*(?:public|protected)\s+(?:(?:static|final|abstract|synchronized|native|default|strictfp|volatile|transient)\s+)*(\S[^;=()]*?)\s*(\w+)\s*(\(|;|=)')


def is_javadoc_block_end(line):
    """判断某行是否为一个 javadoc 块注释（/** ... */）的结束行。"""
    stripped = line.strip()
    return stripped == '*/' or stripped.endswith('*/')


def bracket_depth(line):
    """计算一行中未闭合的 ( { 括号深度（忽略字符串与转义内容）。"""
    depth = 0
    in_string = False
    i = 0
    while i < len(line):
        c = line[i]
        if in_string:
            if c == '\\':
                i += 2
                continue
            if c == '"':
                in_string = False
        else:
            if c == '"':
                in_string = True
            elif c == '(' or c == '{':
                depth += 1
            elif c == ')' or c == '}':
                depth -= 1
        i += 1
    return depth


class FileScanner:
    def __init__(self, path):
        self.path = path
        with open(path, encoding='utf-8') as f:
            self.lines = f.read().splitlines()

    def _tokenize(self):
        """
        逐行扫描，返回三个信息：
        - block_kinds[i]：第 i 行处于哪种块注释中（None 表示不在块注释中，'javadoc' 或 'normal'）
        - javadoc_end_lines：javadoc 块注释结束行的集合
        - annotation_lines：属于注解块（含多行注解）的行的集合
        """
        n = len(self.lines)
        block_kinds = [None] * n
        javadoc_end_lines = set()
        annotation_lines = set()
        in_block = None  # 'javadoc' / 'normal'
        for i, line in enumerate(self.lines):
            if in_block:
                block_kinds[i] = in_block
                if '*/' in line:
                    # 检查结束位置在该行
                    if is_javadoc_block_end(line) and in_block == 'javadoc':
                        javadoc_end_lines.add(i)
                    in_block = None
                    # 该行结束块注释后可能又有新的内容，简单处理：忽略后续同行的 /*（极罕见）
                continue
            # 不在块注释中，检查行内是否开始块注释
            s = line
            j = 0
            while True:
                k = s.find('/*', j)
                if k == -1:
                    break
                if is_in_string(s, k):
                    j = k + 2
                    continue
                # 找到块注释开始
                kind = 'javadoc' if k + 2 < len(s) and s[k + 2] == '*' else 'normal'
                rest = s[k + 2:]
                if '*/' in rest:
                    # 同行结束
                    if is_javadoc_block_end(rest.split('*/')[0] + '*/') and kind == 'javadoc':
                        javadoc_end_lines.add(i)
                    j = s.find('*/', k) + 2
                else:
                    in_block = kind
                    block_kinds[i] = kind
                    break
            if block_kinds[i]:
                continue
            # 注解块检测：以 @ 开头（含多行注解，如 @SuppressWarnings({...})）
            if line.strip().startswith('@'):
                annotation_lines.add(i)
                depth = bracket_depth(line)
                while depth > 0 and i + 1 < n:
                    i += 1
                    annotation_lines.add(i)
                    depth += bracket_depth(self.lines[i])
        return block_kinds, javadoc_end_lines, annotation_lines

    def scan(self):
        block_kinds, javadoc_end_lines, annotation_lines = self._tokenize()
        issues = []
        in_enum = False
        for i, line in enumerate(self.lines):
            stripped = line.strip()
            if block_kinds[i]:
                continue
            if not stripped or stripped.startswith('//'):
                continue
            if stripped.startswith('/*') or stripped.startswith('*') or stripped.endswith('*/'):
                continue
            # 跟踪是否在枚举体内（简化：出现 "enum X {")
            if 'enum ' in stripped and '{' in stripped:
                in_enum = True
            if in_enum and '}' in stripped:
                in_enum = False
            # 跳过注解、注解值、方法体等
            if stripped.startswith('@'):
                continue
            m = CLASS_RE.match(line)
            if m:
                if not self._has_javadoc(i, javadoc_end_lines, annotation_lines):
                    issues.append((i + 1, 'class/interface/enum', m.group(1)))
                continue
            m = DECL_RE.match(line)
            if m:
                # 判断是方法还是字段
                decl_type, name, sig = m.group(1), m.group(2), m.group(3)
                if sig == '(':
                    kind = 'method'
                else:
                    kind = 'field'
                if in_enum and kind == 'field':
                    # 枚举体内的普通字段（非枚举常量）通常少见，也标注
                    kind = 'field'
                if self._is_override(i, annotation_lines):
                    continue
                if not self._has_javadoc(i, javadoc_end_lines, annotation_lines):
                    issues.append((i + 1, kind, name))
        return issues

    def _is_override(self, i, annotation_lines):
        """跳过注解块后判断是否存在 @Override。"""
        j = i - 1
        while j >= 0:
            if j in annotation_lines:
                if self.lines[j].strip().startswith('@Override'):
                    return True
                j -= 1
                continue
            s = self.lines[j].strip()
            if not s or s.startswith('//'):
                j -= 1
                continue
            return False
        return False

    def _has_javadoc(self, i, javadoc_end_lines, annotation_lines):
        j = i - 1
        while j >= 0:
            if j in annotation_lines:
                j -= 1
                continue
            s = self.lines[j].strip()
            if not s:
                j -= 1
                continue
            if s.startswith('//'):
                j -= 1
                continue
            if s.startswith('/*'):
                # 普通块注释开始，无 javadoc
                return False
            return j in javadoc_end_lines
        return False


def is_in_string(line, pos):
    """判断 line[pos] 是否处于字符串/字符字面量内（粗略）。"""
    quote = None
    i = 0
    while i < len(line):
        c = line[i]
        if quote:
            if c == '\\':
                i += 2
                continue
            if c == quote:
                quote = None
        else:
            if c in ('"', "'"):
                quote = c
        if i == pos:
            return quote is not None
        i += 1
    return False


def main():
    java_files = []
    for root, dirs, files in os.walk(ROOT):
        for f in files:
            if f.endswith('.java'):
                java_files.append(os.path.join(root, f))
    java_files.sort()
    total_files = 0
    total_issues = 0
    print(f'=== 共 {len(java_files)} 个 Java 文件 ===')
    for path in java_files:
        fs = FileScanner(path)
        issues = fs.scan()
        if issues:
            total_files += 1
            total_issues += len(issues)
            rel = os.path.relpath(path, ROOT)
            print(f'\n[{rel}]')
            for lineno, kind, name in issues:
                print(f'  {lineno}: {kind} {name}')
    print(f'\n=== 缺失 javadoc 的文件数: {total_files}，缺失声明数: {total_issues} ===')


if __name__ == '__main__':
    main()
