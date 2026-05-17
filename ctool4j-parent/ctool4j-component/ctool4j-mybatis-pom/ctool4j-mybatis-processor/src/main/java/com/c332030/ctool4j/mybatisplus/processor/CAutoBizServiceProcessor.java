package com.c332030.ctool4j.mybatisplus.processor;

import com.c332030.ctool4j.definition.annotation.CBizId;
import lombok.val;
import lombok.var;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * <p>
 * Description: CAutoBizService 注解处理器
 * </p>
 *
 * @since 2025/05/16
 */
@SupportedAnnotationTypes("com.c332030.ctool4j.mybatisplus.processor.CAutoBizService")
public class CAutoBizServiceProcessor extends AbstractProcessor {

    private String template;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.template = loadTemplate("/templates/service.ftl");
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
                    val classElement = (TypeElement) element;
                    generateServiceInterface(classElement);
                }
            }
        }
        return true;
    }

    private void generateServiceInterface(TypeElement entityElement) {
        val bizIdField = findBizIdField(entityElement);
        if (bizIdField == null) {
            return;
        }

        val entityName = entityElement.getSimpleName().toString();
        val entityPackage = getPackageName(entityElement);
        val entityFullName = entityElement.getQualifiedName().toString();

        val bizIdCapital = capitalize(bizIdField);

        val serviceName = "I" + entityName + "Service";
        val servicePackage = getSiblingPackage(entityPackage, "service");
        val serviceFullName = servicePackage + "." + serviceName;

        val code = render(template,
                "packageName", servicePackage,
                "serviceName", serviceName,
                "entityName", entityName,
                "entityFullName", entityFullName,
                "bizIdField", bizIdField,
                "BizIdCapital", bizIdCapital
        );

        try {
            val sourceFile = processingEnv.getFiler()
                    .createSourceFile(serviceFullName, entityElement);
            try (val writer = sourceFile.openWriter()) {
                writer.write(code);
            }
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                    "Generated: " + serviceFullName);
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Failed to generate " + serviceFullName + ": " + e.getMessage());
        }
    }

    private String findBizIdField(TypeElement classElement) {

        val bizIdFields = new ArrayList<>();
        for (val enclosed : classElement.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.FIELD) {
                val bizIdAnno = enclosed.getAnnotation(CBizId.class);
                if (bizIdAnno != null) {
                    bizIdFields.add(enclosed.getSimpleName().toString());
                }
            }
        }

        if (bizIdFields.isEmpty()) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "No @CBizId field found in " + classElement.getQualifiedName());
            return null;
        }

        if (bizIdFields.size() > 1) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Multiple @CBizId fields found in " + classElement.getQualifiedName() +
                            ": " + bizIdFields + ", only one is allowed");
            return null;
        }

        return bizIdFields.get(0);
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

    private String getSiblingPackage(String entityPackage, String sibling) {
        val lastDot = entityPackage.lastIndexOf('.');
        if (lastDot > 0) {
            return entityPackage.substring(0, lastDot) + "." + sibling;
        }
        return sibling;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    private String render(String template, String... kv) {
        var result = template;
        for (int i = 0; i < kv.length; i += 2) {
            val key = kv[i];
            val value = kv[i + 1];
            result = result.replace("${" + key + "}", value);
        }
        return result;
    }
}
