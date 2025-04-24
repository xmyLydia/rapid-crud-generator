package com.rapidcrud.generator.controller;

import com.rapidcrud.generator.dto.CodeGenRequest;
import com.rapidcrud.generator.kafka.AuditLogEvent;
import com.rapidcrud.generator.kafka.KafkaProducerService;
import com.rapidcrud.generator.service.CodeGeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class GeneratorController {

    public static final String MONGO = "mongo";
    private final CodeGeneratorService generatorService;

    @Autowired
    public GeneratorController(CodeGeneratorService generatorService) {
        this.generatorService = generatorService;
    }

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Operation(summary = "Generate full-stack CRUD code", description = "Accepts a JSON schema and generates backend + frontend code with optional Mongo support")
    @ApiResponse(responseCode = "200", description = "Success message or error info")
    @PostMapping("/generate")
    public String generate(@RequestBody CodeGenRequest request) {
        try {
            FileUtils.deleteDirectory(new File("output"));

            String type = request.getType();
            boolean useMongo = "mongo".equalsIgnoreCase(type);

            Map<String, Map<String, String>> schema = request.getSchema();
            if (schema == null || schema.isEmpty()) {
                return "❌ No schema provided.";
            }

            List<String> classNames = new ArrayList<>();
            generatorService.copyAngularProjectTemplate();

            for (Map.Entry<String, Map<String, String>> entry : schema.entrySet()) {
                String className = entry.getKey();
                Map<String, String> fields = entry.getValue();

                generatorService.generateEntity(className, fields, useMongo);
                generatorService.generateRepository(className, useMongo);
                generatorService.generateController(className, useMongo);
                generatorService.generateAngularModule(className, fields);

                classNames.add(className.toLowerCase());
            }

            generatorService.generateAngularRootModule(classNames);
            generatorService.generateAngularAppComponent(classNames);

            String zip = generatorService.zipGeneratedCode(classNames);
            //kafka send audit log event
            AuditLogEvent event = new AuditLogEvent(
                    "GENERATE",
                    String.join(",", classNames),
                    request.toString(),
                    LocalDateTime.now()
            );
            kafkaProducerService.sendLog(event);

            return "✅ Code (" + (useMongo ? "MongoDB" : "SQL") + ") for [" + String.join(", ", classNames) + "] generated and zipped at: " + zip;

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Code generation failed: " + e.getMessage();
        }
    }


}
