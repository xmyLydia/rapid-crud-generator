package com.rapidcrud.generator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

@Data
@Schema(description = "Schema generation request")
public class CodeGenRequest {

    @Schema(description = "Backend type: 'sql' or 'mongo'", example = "mongo")
    private String type = "sql";

    @Schema(
            description = "Entity schema to generate code for",
            example = """
        {
          "User": {
            "name": "String",
            "email": "String"
          },
          "Product": {
            "title": "String",
            "price": "Double"
          }
        }
        """
    )
    private Map<String, Map<String, String>> schema;
}
