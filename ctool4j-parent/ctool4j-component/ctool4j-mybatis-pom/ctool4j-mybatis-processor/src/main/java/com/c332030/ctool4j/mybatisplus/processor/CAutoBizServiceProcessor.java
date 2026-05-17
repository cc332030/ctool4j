package com.c332030.ctool4j.mybatisplus.processor;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
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
        this.template = loadTemplate("/templates/biz-service.ftl");
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
        for (TypeElement annotation : annotations) {
            for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                if (element.getKind() == ElementKind.INTERFACE) {
                    TypeElement interfaceElement = (TypeElement) element;
                    generateServiceInterface(interfaceElement);
                }
            }
        }
        return true;
    }

    private void generateServiceInterface(TypeElement interfaceElement) {
        String bizIdField = findBizIdField(interfaceElement);
        if (bizIdField == null) {
            return;
        }

        String entityName = interfaceElement.getSimpleName().toString();
        String entityPackage = getPackageName(interfaceElement);
        String entityFullName = interfaceElement.getQualifiedName().toString();

        String bizIdCapital = capitalize(bizIdField);

        String serviceName = "I" + entityName.replaceFirst("^I", "") + "Service";
        String servicePackage = getSiblingPackage(entityPackage, "service");
        String serviceFullName = servicePackage + "." + serviceName;

        String code = render(template,
                "packageName", servicePackage,
                "serviceName", serviceName,
                "entityName", entityName,
                "entityFullName", entityFullName,
                "bizIdField", bizIdField,
                "BizIdCapital", bizIdCapital
        );

        try {
            JavaFileObject sourceFile = processingEnv.getFiler()
                    .createSourceFile(serviceFullName, interfaceElement);
            try (Writer writer = sourceFile.openWriter()) {
                writer.write(code);
            }
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                    "Generated: " + serviceFullName);
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Failed to generate " + serviceFullName + ": " + e.getMessage());
        }
    }

    private String findBizIdField(TypeElement interfaceElement) {
        List<String> bizIdMethods = new ArrayList<>();

        for (Element enclosed : interfaceElement.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.METHOD) {
                ExecutableElement method = (ExecutableElement) enclosed;
                String methodName = method.getSimpleName().toString();

                if (methodName.startsWith("get")
                        && method.getParameters().isEmpty()
                        && method.getReturnType().getKind() == TypeKind.DECLARED) {

                    String returnType = method.getReturnType().toString();
                    if (returnType.equals("java.lang.String")) {
                        String fieldName = uncapitalize(methodName.substring(3));
                        bizIdMethods.add(fieldName);
                    }
                }
            }
        }

        if (bizIdMethods.isEmpty()) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "No getter method returning String found in " + interfaceElement.getQualifiedName());
            return null;
        }

        if (bizIdMethods.size() > 1) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Multiple getter methods returning String found in " + interfaceElement.getQualifiedName() +
                            ": " + bizIdMethods + ", only one is allowed");
            return null;
        }

        return bizIdMethods.get(0);
    }

    private String loadTemplate(String path) {
        try (InputStream is = getClass().getResourceAsStream(path);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
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
        Element enclosing = element.getEnclosingElement();
        if (enclosing.getKind() == ElementKind.PACKAGE) {
            return ((PackageElement) enclosing).getQualifiedName().toString();
        }
        return "";
    }

    private String getSiblingPackage(String entityPackage, String sibling) {
        int lastDot = entityPackage.lastIndexOf('.');
        if (lastDot > 0) {
            return entityPackage.substring(0, lastDot) + "." + sibling;
        }
        return sibling;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    private String uncapitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    private String render(String template, String... kv) {
        String result = template;
        for (int i = 0; i < kv.length; i += 2) {
            String key = kv[i];
            String value = kv[i + 1];
            result = result.replace("${" + key + "}", value);
        }
        return result;
    }
}
