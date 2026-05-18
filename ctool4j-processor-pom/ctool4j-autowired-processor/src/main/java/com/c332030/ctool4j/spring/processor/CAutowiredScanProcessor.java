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
import java.util.stream.Collectors;

@SupportedAnnotationTypes("com.c332030.ctool4j.spring.annotation.CAutowiredScan")
public class CAutowiredScanProcessor extends AbstractProcessor {

    private String template;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.template = loadTemplate("/templates/autowired-init.ftl");
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

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
                .map(f -> f.fieldType + " " + f.fieldName)
                .collect(Collectors.joining(",\n        "))
                ;

        val constructorAssignments = autowiredFields.stream()
                .map(f -> className + ".set" + capitalize(f.fieldName) + "(" + f.fieldName + ");")
                .collect(Collectors.joining("\n        "));

        val imports = new StringBuilder();
        imports.append("import ").append(classFullName).append(";\n");
        for (val field : autowiredFields) {
            if (!field.fieldType.startsWith("java.lang.") && !field.fieldType.equals(classFullName)) {
                imports.append("import ").append(field.fieldType).append(";\n");
            }
        }

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

    private static class FieldInfo {
        final String fieldName;
        final String fieldType;

        FieldInfo(String fieldName, String fieldType) {
            this.fieldName = fieldName;
            this.fieldType = fieldType;
        }
    }
}
