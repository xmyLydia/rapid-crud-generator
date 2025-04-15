package com.rapidcrud.generator.controller;

import com.rapidcrud.generator.service.CodeGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
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
    public String generate(@RequestBody Map<String, Map<String, String>> schema) {
        try {
            List<String> classNames = new ArrayList<>();

            for (Map.Entry<String, Map<String, String>> entry : schema.entrySet()) {
                String className = entry.getKey();
                Map<String, String> fields = entry.getValue();

                generatorService.generateEntity(className, fields);
                generatorService.generateRepository(className);
                generatorService.generateController(className);
                generatorService.generateAngularModule(className, fields);

                classNames.add(className.toLowerCase());
            }

            // zip after all classes are generated
            String zip = generatorService.zipGeneratedCode(classNames);

            return "✅ Code for [" + String.join(", ", classNames) + "] generated and zipped at: " + zip;
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Code generation failed: " + e.getMessage();
        }
    }
}
