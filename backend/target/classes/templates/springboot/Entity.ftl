package ${packageName};

import lombok.Data;
import java.time.*;
<#-- DEBUG 输出 -->
// Class: ${className}
// Fields: ${fields?size} field(s)
@Data
public class ${className} {
<#list fields as field>
    private ${field.type} ${field.name};
</#list>
}
