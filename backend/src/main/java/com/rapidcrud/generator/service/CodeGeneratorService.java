package com.rapidcrud.generator.service;

import com.rapidcrud.generator.utils.ZipUtils;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import freemarker.template.Configuration;

@Service
public class CodeGeneratorService {
    private final Configuration cfg;
    private final String packageBase = "com.rapidcrud.generator";

    public CodeGeneratorService() throws IOException {
        cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setClassLoaderForTemplateLoading(getClass().getClassLoader(), "templates/springboot");
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

        Template template = cfg.getTemplate("Entity.ftl");

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

        Template template = cfg.getTemplate("Repository.ftl");

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

        Template template = cfg.getTemplate("Controller.ftl");

        File outputDir = new File("output/controller");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        try (Writer out = new FileWriter(new File(outputDir, className + "Controller.java"))) {
            template.process(dataModel, out);
        }
    }

    public String zipGeneratedCode(String className) throws IOException {
        File sourceDir = new File("output");
        String zipFileName = "output-" + className.toLowerCase() + ".zip";
        File zipFile = new File(zipFileName);

        if (zipFile.exists()) zipFile.delete(); // 防止旧文件干扰

        ZipUtils.zipDirectory(sourceDir, zipFile);
        System.out.println("✅ Generated zip: " + zipFile.getAbsolutePath());
        return zipFileName;
    }
}
