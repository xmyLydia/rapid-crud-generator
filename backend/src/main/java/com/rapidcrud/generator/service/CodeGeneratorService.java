package com.rapidcrud.generator.service;

import com.rapidcrud.generator.utils.ZipUtils;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

    public void generateEntity(String className, Map<String, String> fields, boolean useMongo) throws Exception {
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

        String templatePath = useMongo ? "mongo/mongo-entity.ftl" : "springboot/Entity.ftl";

        Template template = cfg.getTemplate(templatePath);

        File outputDir =  new File(getOutputBase(useMongo) + "/entity");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        try (Writer out = new FileWriter(new File(outputDir, className + ".java"))) {
            template.process(dataModel, out);
        }
    }


    public void generateRepository(String className, boolean useMongo) throws Exception {
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("packageName", packageBase + ".repository");
        dataModel.put("className", className);
        dataModel.put("entityPackage", packageBase + ".entity");

        String templatePath = useMongo ? "mongo/mongo-repository.ftl" : "springboot/Repository.ftl";
        Template template = cfg.getTemplate(templatePath);
        File outputDir =  new File(getOutputBase(useMongo) + "/repository");

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        try (Writer out = new FileWriter(new File(outputDir, className + "Repository.java"))) {
            template.process(dataModel, out);
        }
    }

    public void generateController(String className, boolean useMongo) throws Exception {
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("packageName", packageBase + ".controller");
        dataModel.put("className", className);
        dataModel.put("repositoryPackage", packageBase + ".repository");
        dataModel.put("entityPackage", packageBase + ".entity");

        Template template = cfg.getTemplate("springboot/Controller.ftl");

        File outputDir = new File(getOutputBase(useMongo) + "/controller");
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

    public void generateAngularRootModule(List<String> classNames) throws Exception {
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("modules", classNames);

        String targetDir = "output/frontend/src/app";

        // 生成 app.module.ts
        Template moduleTemplate = cfg.getTemplate("angular/app.module.ts.ftl");
        try (Writer writer = new FileWriter(new File(targetDir, "app.module.ts"))) {
            moduleTemplate.process(dataModel, writer);
        }

        // 生成 app-routing.module.ts
        Template routingTemplate = cfg.getTemplate("angular/app-routing.module.ts.ftl");
        try (Writer writer = new FileWriter(new File(targetDir, "app-routing.module.ts"))) {
            routingTemplate.process(dataModel, writer);
        }

        System.out.println("✅ Generated Angular app.module.ts and app-routing.module.ts");
    }

    public void generateAngularAppComponent(List<String> classNames) throws Exception {
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("modules", classNames);

        String targetDir = "output/frontend/src/app";

        Template tsTemplate = cfg.getTemplate("angular/app.component.ts.ftl");
        try (Writer writer = new FileWriter(new File(targetDir, "app.component.ts"))) {
            tsTemplate.process(dataModel, writer);
        }

        Template htmlTemplate = cfg.getTemplate("angular/app.component.html.ftl");
        try (Writer writer = new FileWriter(new File(targetDir, "app.component.html"))) {
            htmlTemplate.process(dataModel, writer);
        }

        System.out.println("✅ Generated AppComponent");
    }

    public void copyAngularProjectTemplate() throws IOException {
        File source = new File("frontend-template");
        File destination = new File("output/frontend");

        // 删除旧的 frontend 文件夹
        if (destination.exists()) {
            FileUtils.deleteDirectory(destination);
        }

        // 拷贝模板结构
        FileUtils.copyDirectory(source, destination);
        System.out.println("✅ Copied Angular base project to output/frontend/");
    }

    private String getOutputBase(boolean useMongo) {
        return useMongo ? "output/backend-mongo" : "output/backend";
    }
}
