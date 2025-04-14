package ${packageName};

import ${repositoryPackage}.${className}Repository;
import ${entityPackage}.${className};
import org.springframework.web.bind.annotation.*;
import java.util.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/${className?lower_case}")
@RequiredArgsConstructor
public class ${className}Controller {

    private final ${className}Repository repository;

    @GetMapping
    public List<${className}> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public ${className} create(@RequestBody ${className} entity) {
        return repository.save(entity);
    }
}
