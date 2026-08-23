"""
clean-tmp.py - 删除 tmp 目录下文件/目录的安全清理脚本，严格限制不超出 tmp 范围

用法（在项目根执行）:
  python script/clean-tmp.py                清空 tmp 下全部内容
  python script/clean-tmp.py a.log          删除 tmp/a.log
  python script/clean-tmp.py sub b.log      删除多个

说明:
  - 传入参数视为相对 tmp 根目录的路径（如 a.log、sub/b.log）
  - 无参数时清空 tmp 下全部内容
  - 每个目标路径都会拼接到 tmp 根下，经 os.path.realpath 规范化后校验，
    必须位于 tmp 目录内；任何路径穿越（../、/、绝对路径指向 tmp 外）都会被拒绝，
    防止误删 tmp 之外的文件
"""
import argparse
import os
import shutil
import sys


def tmp_root() -> str:
    """tmp 根目录（脚本位于 script/，tmp 在项目根）"""
    script_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.dirname(script_dir)
    root = os.path.join(project_root, "tmp")
    os.makedirs(root, exist_ok=True)
    return root


def resolve_safe(root: str, target: str) -> str:
    """
    将相对 tmp 的路径规范化为绝对路径，并确保位于 tmp 内；越界则抛错。
    os.path.realpath 会解析 ..、.、绝对路径、符号链接，
    因此任何路径穿越（如 ../ 跳出 tmp、/ 指向根、盘符绝对路径）都会被规范化后识别出越界。
    """
    if not target:
        raise ValueError("空路径被拒绝")
    full = os.path.realpath(os.path.join(root, target))
    real_root = os.path.realpath(root) + os.sep
    if not full.startswith(real_root):
        raise ValueError(f"路径超出 tmp 范围: '{target}' -> '{full}'")
    return full


def remove_item(full: str) -> None:
    """删除文件或目录（目录递归删除）"""
    if os.path.isdir(full) and not os.path.islink(full):
        shutil.rmtree(full)
    else:
        os.remove(full)


def main() -> int:
    parser = argparse.ArgumentParser(description="Safe clean tmp")
    parser.add_argument("targets", nargs="*", help="relative paths under tmp")
    args = parser.parse_args()

    root = tmp_root()

    if not args.targets:
        # 无参数：清空 tmp 下全部内容（均为临时产物）
        print(f"Cleaning all under: {root}")
        for entry in os.listdir(root):
            remove_item(os.path.join(root, entry))
        print("tmp cleaned.")
        return 0

    for target in args.targets:
        try:
            full = resolve_safe(root, target)
        except ValueError as e:
            print(f"REJECTED: {e}")
            continue
        if os.path.lexists(full):
            remove_item(full)
            print(f"deleted: {full}")
        else:
            print(f"skip (not exist): {full}")

    return 0


if __name__ == "__main__":
    sys.exit(main())
