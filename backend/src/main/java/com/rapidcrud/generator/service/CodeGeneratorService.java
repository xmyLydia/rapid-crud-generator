package com.rapidcrud.generator.service;

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

    public CodeGeneratorService() throws IOException {
        cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setClassLoaderForTemplateLoading(getClass().getClassLoader(), "templates/springboot");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    public void generateEntity(String className, Map<String, String> fields) throws Exception {
        // 准备数据模型
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("packageName", "com.rapidadmin.generated");
        dataModel.put("className", className);

        List<Map<String, String>> fieldList = new ArrayList<>();
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            Map<String, String> field = new HashMap<>();
            field.put("name", entry.getKey());
            field.put("type", entry.getValue());
            fieldList.add(field);
        }
        dataModel.put("fields", fieldList);

        // 加载模板
        Template template = cfg.getTemplate("Entity.ftl");

        // 创建输出目录
        File outputDir = new File("output");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // 输出生成的 Java 文件
        File outputFile = new File(outputDir, className + ".java");
        try (Writer writer = new FileWriter(outputFile)) {
            template.process(dataModel, writer);
        }

        System.out.println("✅ Generated " + className + ".java to /output");
    }
}
