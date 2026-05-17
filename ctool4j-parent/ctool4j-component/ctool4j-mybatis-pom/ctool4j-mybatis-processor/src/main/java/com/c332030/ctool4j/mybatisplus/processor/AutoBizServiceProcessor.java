package com.c332030.ctool4j.mybatisplus.processor;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * <p>
 * Description: AutoBizService 注解处理器
 * </p>
 *
 * @since 2025/05/16
 */
@SupportedAnnotationTypes("com.c332030.ctool4j.mybatisplus.processor.AutoBizService")
public class AutoBizServiceProcessor extends AbstractProcessor {

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
        for (TypeElement annotation : annotations) {
            for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                if (element.getKind() == ElementKind.CLASS) {
                    TypeElement classElement = (TypeElement) element;
                    AutoBizService anno = classElement.getAnnotation(AutoBizService.class);
                    if (anno != null) {
                        generateServiceInterface(classElement, anno);
                    }
                }
            }
        }
        return true;
    }

    private void generateServiceInterface(TypeElement entityElement, AutoBizService anno) {
        String entityName = entityElement.getSimpleName().toString();
        String entityPackage = getPackageName(entityElement);
        String entityFullName = entityElement.getQualifiedName().toString();
        
        String bizIdField = anno.bizIdField();
        String bizIdCapital = capitalize(bizIdField);
        
        String serviceName = "I" + entityName + anno.serviceSuffix();
        String servicePackage = entityPackage + ".service";
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
                    .createSourceFile(serviceFullName, entityElement);
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

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
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
