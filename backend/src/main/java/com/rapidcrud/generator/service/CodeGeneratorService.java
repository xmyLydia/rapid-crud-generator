package com.rapidcrud.generator.service;

import com.rapidcrud.generator.utils.ZipUtils;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.*;

import freemarker.template.Configuration;

@Service
public class CodeGeneratorService {
    private final Configuration cfg;
    private final String packageBase = "com.rapidcrud.generator";

    public CodeGeneratorService() throws IOException {
        cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setClassLoaderForTemplateLoading(getClass().getClassLoader(), "templates");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    public void generateEntity(String className, Map<String, String> fields) throws Exception {
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("packageName", packageBase + ".entity");
        dataModel.put("className", className);

        List<Map<String, String>> fieldList = new ArrayList<>();
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            Map<String, String> field = new HashMap<>();
            field.put("name", entry.getKey());
            field.put("type", entry.getValue());
            fieldList.add(field);
        }
        dataModel.put("fields", fieldList);

        Template template = cfg.getTemplate("springboot/Entity.ftl");

        File outputDir = new File("output/entity");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        try (Writer out = new FileWriter(new File(outputDir, className + ".java"))) {
            template.process(dataModel, out);
        }
    }


    public void generateRepository(String className) throws Exception {
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("packageName", packageBase + ".repository");
        dataModel.put("className", className);
        dataModel.put("entityPackage", packageBase + ".entity");

        Template template = cfg.getTemplate("springboot/Repository.ftl");

        File outputDir = new File("output/repository");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        try (Writer out = new FileWriter(new File(outputDir, className + "Repository.java"))) {
            template.process(dataModel, out);
        }
    }

    public void generateController(String className) throws Exception {
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("packageName", packageBase + ".controller");
        dataModel.put("className", className);
        dataModel.put("repositoryPackage", packageBase + ".repository");
        dataModel.put("entityPackage", packageBase + ".entity");

        Template template = cfg.getTemplate("springboot/Controller.ftl");

        File outputDir = new File("output/controller");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        try (Writer out = new FileWriter(new File(outputDir, className + "Controller.java"))) {
            template.process(dataModel, out);
        }
    }

    public String zipGeneratedCode(List<String> classNames) throws IOException {
        File sourceDir = new File("output");

        String joinedNames = String.join("_", classNames);
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String zipFileName = "generated_" + joinedNames + "_" + timestamp + ".zip";
        File zipFile = new File(zipFileName);

        if (zipFile.exists()) zipFile.delete();

        ZipUtils.zipDirectory(sourceDir, zipFile);
        System.out.println("✅ Zipped all modules to: " + zipFile.getAbsolutePath());

        return zipFile.getAbsolutePath();
    }

    public void generateAngularModule(String className, Map<String, String> fields) throws Exception {
        List<Map<String, String>> fieldList = new ArrayList<>();
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            Map<String, String> field = new HashMap<>();
            field.put("name", entry.getKey());
            field.put("type", entry.getValue());
            fieldList.add(field);
        }

        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("className", className);
        dataModel.put("fields", fieldList);

        String folder = "output/frontend/src/app/" + className.toLowerCase();
        new File(folder).mkdirs();

        // 模板列表（模板名，目标文件名）
        Map<String, String> files = Map.of(
                "table.component.ts.ftl", className.toLowerCase() + "-table.component.ts",
                "table.component.html.ftl", className.toLowerCase() + "-table.component.html",
                "form.component.ts.ftl", className.toLowerCase() + "-form.component.ts",
                "form.component.html.ftl", className.toLowerCase() + "-form.component.html",
                "service.ts.ftl", className.toLowerCase() + ".service.ts",
                "module.ts.ftl", className.toLowerCase() + ".module.ts"
        );

        for (Map.Entry<String, String> entry : files.entrySet()) {
            Template template = cfg.getTemplate("angular/" + entry.getKey());
            try (Writer writer = new FileWriter(new File(folder, entry.getValue()))) {
                template.process(dataModel, writer);
            }
        }

        System.out.println("✅ Generated Angular module for " + className);
    }

}
