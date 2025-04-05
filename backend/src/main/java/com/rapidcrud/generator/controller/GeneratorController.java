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
                String className = entry.getKey();
                Map<String, String> fields = entry.getValue();
                generatorService.generateEntity(className, fields);
            }
            return "✅ Code generation complete! Check the /output folder.";
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Code generation failed: " + e.getMessage();
        }
    }
}
