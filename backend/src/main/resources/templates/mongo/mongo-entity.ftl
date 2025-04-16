package com.rapidadmin.generated;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "${className?lower_case}")
public class ${className} {
    @Id
    private String id;

<#list fields as field>
    private ${field.type} ${field.name};
</#list>
}
