package ${packageName};

import org.springframework.data.jpa.repository.JpaRepository;
import ${entityPackage}.${className};

public interface ${className}Repository extends JpaRepository<${className}, Long> {
}
