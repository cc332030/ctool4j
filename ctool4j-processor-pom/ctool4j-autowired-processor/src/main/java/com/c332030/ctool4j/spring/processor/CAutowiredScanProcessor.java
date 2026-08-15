package com.c332030.ctool4j.spring.processor;

import com.c332030.ctool4j.spring.annotation.CAutowired;
import lombok.val;
import lombok.var;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: CAutowiredScanProcessor
 * </p>
 * <p>
 * 注解处理器：扫描标注 @CAutowiredScan 的类，为其中的 @CAutowired 字段生成构造器注入的 Init 类
 * </p>
 *
 * @since 2026/5/17
 */
@SupportedAnnotationTypes("com.c332030.ctool4j.spring.annotation.CAutowiredScan")
public class CAutowiredScanProcessor extends AbstractProcessor {

    private String template;

    /**
     * 初始化：加载 autowired-init 模板
     *
     * @param processingEnv 处理环境
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.template = loadTemplate("/templates/autowired-init.ftl");
    }

    /**
     * 支持的 Java 源版本
     *
     * @return Java 8
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    /**
     * 处理注解：为标注 @CAutowiredScan 的类生成 Init 类
     *
     * @param annotations 注解集合
     * @param roundEnv    轮次环境
     * @return true
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (template == null) {
            return true;
        }
        for (val annotation : annotations) {
            for (val element : roundEnv.getElementsAnnotatedWith(annotation)) {
                if (element.getKind() == ElementKind.CLASS) {
                    TypeElement classElement = (TypeElement) element;
                    generateInitClass(classElement);
                }
            }
        }
        return true;
    }

    private void generateInitClass(TypeElement classElement) {
        val autowiredFields = findAutowiredFields(classElement);
        if (autowiredFields.isEmpty()) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                    "No @CAutowired fields found in " + classElement.getQualifiedName());
            return;
        }

        val className = classElement.getSimpleName().toString();
        val classPackage = getPackageName(classElement);
        val classFullName = classElement.getQualifiedName().toString();

        val initClassName = className + "Init";
        val initClassPackage = classPackage;
        val initClassFullName = initClassPackage + "." + initClassName;

        val constructorParams = autowiredFields.stream()
                .map(f -> toSimpleType(f.fieldType) + " " + f.fieldName)
                .collect(Collectors.joining(",\n        "))
                ;

        val constructorAssignments = autowiredFields.stream()
                .map(f -> className + ".set" + capitalize(f.fieldName) + "(" + f.fieldName + ");")
                .collect(Collectors.joining("\n        "));

        // 泛型/数组字段须拆出内部完整类名逐一 import（如 java.util.List<com.xxx.Xxx> 需 import 两者），
        // 不能直接 import 整个类型字符串（java.util.List<com.xxx.Xxx> 是非法 import）
        val imports = new StringBuilder();
        imports.append("import ").append(classFullName).append(";\n");
        autowiredFields.stream()
                .flatMap(field -> extractFullClassNames(field.fieldType).stream())
                .distinct()
                .filter(importName -> !importName.startsWith("java.lang."))
                .filter(importName -> !importName.equals(classFullName))
                .forEach(importName -> imports.append("import ").append(importName).append(";\n"));

        val code = render(template,
                "packageName", initClassPackage,
                "initClassName", initClassName,
                "className", className,
                "imports", imports.toString(),
                "constructorParams", constructorParams,
                "constructorAssignments", constructorAssignments
        );

        try {
            val sourceFile = processingEnv.getFiler()
                    .createSourceFile(initClassFullName, classElement);
            try (val writer = sourceFile.openWriter()) {
                writer.write(code);
            }
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                    "Generated: " + initClassFullName);
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Failed to generate " + initClassFullName + ": " + e.getMessage());
        }
    }

    private List<FieldInfo> findAutowiredFields(TypeElement classElement) {
        val fields = new ArrayList<FieldInfo>();
        for (val enclosed : classElement.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.FIELD) {

                val field = (VariableElement) enclosed;
                val hasAutowired = field.getAnnotationMirrors().stream()
                        .anyMatch(am -> am.getAnnotationType().toString()
                                .equals(CAutowired.class.getCanonicalName()));
                if (hasAutowired) {
                    val fieldName = field.getSimpleName().toString();
                    val fieldType = field.asType().toString();
                    fields.add(new FieldInfo(fieldName, fieldType));
                }
            }
        }
        return fields;
    }

    private String loadTemplate(String path) {
        try (val is = getClass().getResourceAsStream(path);
             val reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            val sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private String getPackageName(TypeElement element) {
        val enclosing = element.getEnclosingElement();
        if (enclosing.getKind() == ElementKind.PACKAGE) {
            return ((PackageElement) enclosing).getQualifiedName().toString();
        }
        return "";
    }

    private String render(String template, String... kv) {
        var result = template;
        for (var i = 0; i < kv.length; i += 2) {
            val key = kv[i];
            val value = kv[i + 1];
            result = result.replace("${" + key + "}", value);
        }
        return result;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * 将完整类型名（含泛型/数组）转为简单名类型声明，如：
     * java.util.List&lt;com.xxx.XxxService&gt; → List&lt;XxxService&gt;；com.xxx.Xxx[] → Xxx[]
     * 每个完整类名取其最后一个 '.' 之后的简单名（如 com.xxx.Xxx → Xxx）
     */
    private String toSimpleType(String fieldType) {
        return extractFullClassNames(fieldType).stream()
                .reduce(fieldType, (type, fullName) -> type.replace(
                        fullName, fullName.substring(fullName.lastIndexOf('.') + 1)));
    }

    /**
     * 提取类型字符串中的所有完整类名（含泛型参数、内嵌类），如：
     * java.util.Map&lt;java.lang.String, com.xxx.Xxx&gt; → [java.util.Map, java.lang.String, com.xxx.Xxx]
     */
    private List<String> extractFullClassNames(String fieldType) {
        val matcher = Pattern.compile("[a-zA-Z_$][a-zA-Z0-9_$]*(\\.[a-zA-Z_$][a-zA-Z0-9_$]*)+").matcher(fieldType);
        val names = new ArrayList<String>();
        while (matcher.find()) {
            names.add(matcher.group());
        }
        return names;
    }

    private static class FieldInfo {
        final String fieldName;
        final String fieldType;

        FieldInfo(String fieldName, String fieldType) {
            this.fieldName = fieldName;
            this.fieldType = fieldType;
        }
    }
}
