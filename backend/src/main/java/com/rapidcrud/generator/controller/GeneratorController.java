package com.rapidcrud.generator.controller;

import com.rapidcrud.generator.service.CodeGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class GeneratorController {

    private final CodeGeneratorService generatorService;

    @Autowired
    public GeneratorController(CodeGeneratorService generatorService) {
        this.generatorService = generatorService;
    }

    @PostMapping("/generate")
    public String generateFromSchema(@RequestBody Map<String, Map<String, String>> schema) {
        try {
            for (Map.Entry<String, Map<String, String>> entry : schema.entrySet()) {
                final String className = entry.getKey();
                final Map<String, String> fields = entry.getValue();
                generatorService.generateEntity(className, fields);
                generatorService.generateRepository(className);
                generatorService.generateController(className);
                final String zip = generatorService.zipGeneratedCode(className);
                return "✅ Entity, Repository and Controller generated in /output, and zipped at " + zip;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Code generation failed: " + e.getMessage();
        }
        return "⚠️ No schema processed.";
    }
}
