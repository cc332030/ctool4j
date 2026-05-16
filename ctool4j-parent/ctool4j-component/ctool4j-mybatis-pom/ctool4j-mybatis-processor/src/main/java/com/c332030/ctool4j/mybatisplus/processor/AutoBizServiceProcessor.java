package com.c332030.ctool4j.mybatisplus.processor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.PackageElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Set;

/**
 * <p>
 * Description: AutoBizService 注解处理器
 * </p>
 *
 * <p>自动生成业务 Service 接口，参照 ICBizBaseService 的方法模式</p>
 *
 * @since 2025/05/16
 */
public class AutoBizServiceProcessor extends AbstractProcessor {

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(AutoBizService.class.getName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                if (element instanceof TypeElement) {
                    TypeElement typeElement = (TypeElement) element;
                    AutoBizService anno = typeElement.getAnnotation(AutoBizService.class);
                    if (anno != null) {
                        generateServiceInterface(typeElement, anno);
                    }
                }
            }
        }
        return true;
    }

    private void generateServiceInterface(TypeElement entityElement, AutoBizService anno) {
        String entityName = entityElement.getSimpleName().toString();
        String packageName = getPackageName(entityElement);
        
        String bizIdField = anno.bizIdField();
        String bizIdColumn = anno.bizIdColumn();
        if (bizIdColumn.isEmpty()) {
            bizIdColumn = camelToSnake(bizIdField);
        }
        
        String serviceSuffix = anno.serviceSuffix();
        String serviceName = "I" + entityName + serviceSuffix;
        
        String servicePackage = packageName + ".service";
        String serviceFullName = servicePackage + "." + serviceName;
        
        String code = generateServiceCode(servicePackage, serviceName, entityName, bizIdField, bizIdColumn);
        
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
        
        if (anno.generateImpl()) {
            generateServiceImpl(entityElement, anno, serviceName, servicePackage);
        }
    }

    private void generateServiceImpl(TypeElement entityElement, AutoBizService anno,
                                     String serviceName, String servicePackage) {
        String entityName = entityElement.getSimpleName().toString();
        String packageName = getPackageName(entityElement);
        
        String bizIdField = anno.bizIdField();
        String bizIdColumn = anno.bizIdColumn();
        if (bizIdColumn.isEmpty()) {
            bizIdColumn = camelToSnake(bizIdField);
        }
        
        String implName = entityName + anno.serviceSuffix() + "Impl";
        String implPackage = packageName + ".service.impl";
        String implFullName = implPackage + "." + implName;
        
        String code = generateServiceImplCode(implPackage, implName, serviceName, 
                servicePackage, entityName, bizIdField, bizIdColumn);
        
        try {
            JavaFileObject sourceFile = processingEnv.getFiler()
                    .createSourceFile(implFullName, entityElement);
            try (Writer writer = sourceFile.openWriter()) {
                writer.write(code);
            }
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                    "Generated: " + implFullName);
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Failed to generate " + implFullName + ": " + e.getMessage());
        }
    }

    private String getPackageName(TypeElement element) {
        Element enclosing = element.getEnclosingElement();
        if (enclosing instanceof PackageElement) {
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
                                       String entityName, String bizIdField, String bizIdColumn) {
        String bizIdCapitalized = capitalize(bizIdField);
        
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(packageName).append(";\n\n");
        
        sb.append("import cn.hutool.core.collection.CollUtil;\n");
        sb.append("import cn.hutool.core.lang.Opt;\n");
        sb.append("import cn.hutool.core.util.StrUtil;\n");
        sb.append("import com.baomidou.mybatisplus.core.toolkit.support.SFunction;\n");
        sb.append("import com.c332030.ctool4j.core.util.CCollUtils;\n");
        sb.append("import com.c332030.ctool4j.core.util.CList;\n");
        sb.append("import com.c332030.ctool4j.core.util.CMap;\n");
        sb.append("import com.c332030.ctool4j.definition.function.CFunction;\n");
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

    private String generateServiceImplCode(String packageName, String implName,
                                           String serviceName, String servicePackage,
                                           String entityName, String bizIdField, String bizIdColumn) {
        String bizIdCapitalized = capitalize(bizIdField);
        
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(packageName).append(";\n\n");
        
        sb.append("import com.baomidou.mybatisplus.core.toolkit.support.SFunction;\n");
        sb.append("import com.c332030.ctool4j.mybatisplus.service.impl.CServiceImpl;\n");
        sb.append("import ").append(servicePackage).append(".").append(serviceName).append(";\n\n");
        
        sb.append("/**\n");
        sb.append(" * <p>\n");
        sb.append(" * Description: ").append(implName).append("\n");
        sb.append(" * </p>\n");
        sb.append(" * <p>\n");
        sb.append(" * Auto-generated by @AutoBizService annotation processor\n");
        sb.append(" * </p>\n");
        sb.append(" *\n");
        sb.append(" * @since auto-generated\n");
        sb.append(" */\n");
        sb.append("public class ").append(implName).append("<M extends CBaseMapper<")
          .append(entityName).append(">> extends CServiceImpl<M, ").append(entityName)
          .append("> implements ").append(serviceName).append("<").append(entityName).append("> {\n\n");
        
        sb.append("    @Override\n");
        sb.append("    public String get").append(bizIdCapitalized).append("(").append(entityName)
          .append(" entity) {\n");
        sb.append("        return entity.get").append(bizIdCapitalized).append("();\n");
        sb.append("    }\n\n");
        
        sb.append("    @Override\n");
        sb.append("    public SFunction<").append(entityName).append(", String> get")
          .append(bizIdCapitalized).append("Column() {\n");
        sb.append("        return ").append(entityName).append("::get").append(bizIdCapitalized).append(";\n");
        sb.append("    }\n\n");
        
        sb.append("    @Override\n");
        sb.append("    public String getBizId(").append(entityName).append(" entity) {\n");
        sb.append("        return get").append(bizIdCapitalized).append("(entity);\n");
        sb.append("    }\n\n");
        
        sb.append("    @Override\n");
        sb.append("    public SFunction<").append(entityName).append(", String> getBizIdColumn() {\n");
        sb.append("        return get").append(bizIdCapitalized).append("Column();\n");
        sb.append("    }\n\n");
        
        sb.append("}\n");
        
        return sb.toString();
    }
}
