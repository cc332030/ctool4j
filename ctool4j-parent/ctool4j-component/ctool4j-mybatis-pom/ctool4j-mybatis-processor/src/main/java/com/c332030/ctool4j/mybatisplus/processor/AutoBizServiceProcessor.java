package com.c332030.ctool4j.mybatisplus.processor;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * <p>
 * Description: AutoBizService 注解处理器
 * </p>
 *
 * <p>编译时生成业务 Service 接口源文件</p>
 *
 * @since 2025/05/16
 */
@SupportedAnnotationTypes("com.c332030.ctool4j.mybatisplus.processor.AutoBizService")
public class AutoBizServiceProcessor extends AbstractProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
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
        String packageName = getPackageName(entityElement);
        String entityFullName = entityElement.getQualifiedName().toString();
        
        String bizIdField = anno.bizIdField();
        String bizIdColumn = anno.bizIdColumn();
        if (bizIdColumn.isEmpty()) {
            bizIdColumn = camelToSnake(bizIdField);
        }
        
        String serviceSuffix = anno.serviceSuffix();
        String serviceName = "I" + entityName + serviceSuffix;
        
        String servicePackage = packageName + ".service";
        String serviceFullName = servicePackage + "." + serviceName;
        
        String code = generateServiceCode(servicePackage, serviceName, entityName, entityFullName, bizIdField, bizIdColumn);
        
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

    private String getPackageName(TypeElement element) {
        Element enclosing = element.getEnclosingElement();
        if (enclosing.getKind() == ElementKind.PACKAGE) {
            return ((PackageElement) enclosing).getQualifiedName().toString();
        }
        return "";
    }

    private String camelToSnake(String camel) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < camel.length(); i++) {
            char c = camel.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    sb.append('_');
                }
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    private String generateServiceCode(String packageName, String serviceName,
                                       String entityName, String entityFullName, String bizIdField, String bizIdColumn) {
        String bizIdCapitalized = capitalize(bizIdField);

        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(packageName).append(";\n\n");

        sb.append("import cn.hutool.core.collection.CollUtil;\n");
        sb.append("import cn.hutool.core.lang.Opt;\n");
        sb.append("import cn.hutool.core.util.StrUtil;\n");
        sb.append("import com.baomidou.mybatisplus.core.toolkit.support.SFunction;\n");
        sb.append("import com.c332030.ctool4j.core.util.CList;\n");
        sb.append("import com.c332030.ctool4j.core.util.CMap;\n");
        sb.append("import com.c332030.ctool4j.mybatisplus.service.ICBizService;\n");
        sb.append("import ").append(entityFullName).append(";\n");
        sb.append("import java.util.Collection;\n");
        sb.append("import java.util.List;\n");
        sb.append("import java.util.Map;\n");
        sb.append("import java.util.Objects;\n\n");

        sb.append("/**\n");
        sb.append(" * <p>\n");
        sb.append(" * Description: ").append(serviceName).append("\n");
        sb.append(" * </p>\n");
        sb.append(" * <p>\n");
        sb.append(" * Auto-generated by @AutoBizService annotation processor\n");
        sb.append(" * </p>\n");
        sb.append(" *\n");
        sb.append(" * @since auto-generated\n");
        sb.append(" */\n");
        sb.append("public interface ").append(serviceName).append("<ENTITY extends ")
          .append(entityName).append("> extends ICBizService<ENTITY, ").append(entityName).append("> {\n\n");

        sb.append("    String get").append(bizIdCapitalized).append("(").append(entityName)
          .append(" entity);\n\n");

        sb.append("    SFunction<ENTITY, String> get").append(bizIdCapitalized).append("Column();\n\n");

        sb.append("    default ENTITY getBy").append(bizIdCapitalized).append("(String ").append(bizIdField)
          .append(") {\n");
        sb.append("        if(StrUtil.isBlank(").append(bizIdField).append(")) {\n");
        sb.append("            return null;\n");
        sb.append("        }\n");
        sb.append("        return getByBizId(").append(bizIdField).append(");\n");
        sb.append("    }\n\n");

        sb.append("    default ENTITY getBy").append(bizIdCapitalized).append("(").append(entityName)
          .append(" entity) {\n");
        sb.append("        if(Objects.isNull(entity)) {\n");
        sb.append("            return null;\n");
        sb.append("        }\n");
        sb.append("        return getBy").append(bizIdCapitalized).append("(get").append(bizIdCapitalized)
          .append("(entity));\n");
        sb.append("    }\n\n");

        sb.append("    default Opt<ENTITY> getBy").append(bizIdCapitalized).append("Opt(String ")
          .append(bizIdField).append(") {\n");
        sb.append("        return Opt.ofNullable(getBy").append(bizIdCapitalized).append("(")
          .append(bizIdField).append("));\n");
        sb.append("    }\n\n");

        sb.append("    default Opt<ENTITY> getBy").append(bizIdCapitalized).append("Opt(")
          .append(entityName).append(" entity) {\n");
        sb.append("        return Opt.ofNullable(getBy").append(bizIdCapitalized).append("(entity));\n");
        sb.append("    }\n\n");

        sb.append("    default List<ENTITY> listBy").append(bizIdCapitalized).append("(String ")
          .append(bizIdField).append(") {\n");
        sb.append("        if(StrUtil.isBlank(").append(bizIdField).append(")) {\n");
        sb.append("            return CList.of();\n");
        sb.append("        }\n");
        sb.append("        return listByBizId(").append(bizIdField).append(");\n");
        sb.append("    }\n\n");

        sb.append("    default List<ENTITY> listBy").append(bizIdCapitalized).append("(")
          .append(entityName).append(" entity) {\n");
        sb.append("        if(null == entity) {\n");
        sb.append("            return CList.of();\n");
        sb.append("        }\n");
        sb.append("        return listBy").append(bizIdCapitalized).append("(get").append(bizIdCapitalized)
          .append("(entity));\n");
        sb.append("    }\n\n");

        sb.append("    default Long countBy").append(bizIdCapitalized).append("(String ")
          .append(bizIdField).append(") {\n");
        sb.append("        if(StrUtil.isBlank(").append(bizIdField).append(")) {\n");
        sb.append("            return 0L;\n");
        sb.append("        }\n");
        sb.append("        return countByBizId(").append(bizIdField).append(");\n");
        sb.append("    }\n\n");

        sb.append("    default Long countBy").append(bizIdCapitalized).append("(").append(entityName)
          .append(" entity) {\n");
        sb.append("        if(entity == null) {\n");
        sb.append("            return 0L;\n");
        sb.append("        }\n");
        sb.append("        return countBy").append(bizIdCapitalized).append("(get").append(bizIdCapitalized)
          .append("(entity));\n");
        sb.append("    }\n\n");

        sb.append("    default boolean updateBy").append(bizIdCapitalized).append("(ENTITY entity) {\n");
        sb.append("        if(Objects.isNull(entity)) {\n");
        sb.append("            return false;\n");
        sb.append("        }\n");
        sb.append("        return updateByBizId(entity);\n");
        sb.append("    }\n\n");

        sb.append("    default boolean removeBy").append(bizIdCapitalized).append("(String ")
          .append(bizIdField).append(") {\n");
        sb.append("        if(StrUtil.isBlank(").append(bizIdField).append(")) {\n");
        sb.append("            return false;\n");
        sb.append("        }\n");
        sb.append("        return removeByBizId(").append(bizIdField).append(");\n");
        sb.append("    }\n\n");

        sb.append("    default boolean removeBy").append(bizIdCapitalized).append("(")
          .append(entityName).append(" entity) {\n");
        sb.append("        if(null == entity) {\n");
        sb.append("            return false;\n");
        sb.append("        }\n");
        sb.append("        return removeBy").append(bizIdCapitalized).append("(get").append(bizIdCapitalized)
          .append("(entity));\n");
        sb.append("    }\n\n");

        sb.append("    default List<ENTITY> listBy").append(bizIdCapitalized).append("s(Collection<String> ")
          .append(bizIdField).append("s) {\n");
        sb.append("        if(CollUtil.isEmpty(").append(bizIdField).append("s)) {\n");
        sb.append("            return CList.of();\n");
        sb.append("        }\n");
        sb.append("        return listByBizIds(").append(bizIdField).append("s);\n");
        sb.append("    }\n\n");

        sb.append("    default List<ENTITY> listBy").append(bizIdCapitalized).append("s(List<? extends ")
          .append(entityName).append("> entityList) {\n");
        sb.append("        if(CollUtil.isEmpty(entityList)) {\n");
        sb.append("            return CList.of();\n");
        sb.append("        }\n");
        sb.append("        return listByBizIds(entityList);\n");
        sb.append("    }\n\n");

        sb.append("    default Long countBy").append(bizIdCapitalized).append("s(Collection<String> ")
          .append(bizIdField).append("s) {\n");
        sb.append("        if(CollUtil.isEmpty(").append(bizIdField).append("s)) {\n");
        sb.append("            return 0L;\n");
        sb.append("        }\n");
        sb.append("        return countByBizIds(").append(bizIdField).append("s);\n");
        sb.append("    }\n\n");

        sb.append("    default Long countBy").append(bizIdCapitalized).append("s(List<? extends ")
          .append(entityName).append("> entityList) {\n");
        sb.append("        if(CollUtil.isEmpty(entityList)) {\n");
        sb.append("            return 0L;\n");
        sb.append("        }\n");
        sb.append("        return countByBizIds(entityList);\n");
        sb.append("    }\n\n");

        sb.append("    default boolean removeBy").append(bizIdCapitalized).append("s(Collection<String> ")
          .append(bizIdField).append("s) {\n");
        sb.append("        if(CollUtil.isEmpty(").append(bizIdField).append("s)) {\n");
        sb.append("            return false;\n");
        sb.append("        }\n");
        sb.append("        return removeByBizIds(").append(bizIdField).append("s);\n");
        sb.append("    }\n\n");

        sb.append("    default boolean removeBy").append(bizIdCapitalized).append("s(List<? extends ")
          .append(entityName).append("> entityList) {\n");
        sb.append("        if(CollUtil.isEmpty(entityList)) {\n");
        sb.append("            return false;\n");
        sb.append("        }\n");
        sb.append("        return removeByBizIds(entityList);\n");
        sb.append("    }\n\n");

        sb.append("    default Map<String, ENTITY> listMapBy").append(bizIdCapitalized)
          .append("s(Collection<String> ").append(bizIdField).append("s) {\n");
        sb.append("        if(CollUtil.isEmpty(").append(bizIdField).append("s)) {\n");
        sb.append("            return CMap.of();\n");
        sb.append("        }\n");
        sb.append("        return listMapByBizIds(").append(bizIdField).append("s);\n");
        sb.append("    }\n\n");

        sb.append("    default Map<String, ENTITY> listMapBy").append(bizIdCapitalized)
          .append("s(List<? extends ").append(entityName).append("> entityList) {\n");
        sb.append("        if(CollUtil.isEmpty(entityList)) {\n");
        sb.append("            return CMap.of();\n");
        sb.append("        }\n");
        sb.append("        return listMapByBizIds(entityList);\n");
        sb.append("    }\n\n");

        sb.append("    default Map<String, List<ENTITY>> listGroupMapBy").append(bizIdCapitalized)
          .append("s(Collection<String> ").append(bizIdField).append("s) {\n");
        sb.append("        if(CollUtil.isEmpty(").append(bizIdField).append("s)) {\n");
        sb.append("            return CMap.of();\n");
        sb.append("        }\n");
        sb.append("        return listGroupMapByBizIds(").append(bizIdField).append("s);\n");
        sb.append("    }\n\n");

        sb.append("}\n");

        return sb.toString();
    }
}
