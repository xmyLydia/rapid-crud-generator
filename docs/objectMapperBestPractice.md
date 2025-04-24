# ✅ ObjectMapper Best Practices in Spring Boot

This guide outlines the best practices for configuring and using `ObjectMapper` in a Spring Boot project, especially when working with `LocalDateTime`, Kafka, and Elasticsearch.

---

## 🔍 Problem Summary

If you encounter the following error:

```
InvalidDefinitionException: Java 8 date/time type `LocalDateTime` not supported by default
```

It typically means that Jackson was not properly configured to support Java 8 date/time classes (e.g., `LocalDateTime`).

---

## ✅ Root Cause

You may have:

- Declared a **new `ObjectMapper()` manually** instead of using the one provided by Spring Boot
- Overridden Spring Boot's default `ObjectMapper` by declaring your own `@Bean`
- Not imported the proper Jackson module (`jackson-datatype-jsr310`)

---

## ✅ Solution Steps

### 1. Add required dependency

In `pom.xml`:

```xml
<dependency>
  <groupId>com.fasterxml.jackson.datatype</groupId>
  <artifactId>jackson-datatype-jsr310</artifactId>
  <version>2.18.3</version> <!-- Match your jackson-databind version -->
</dependency>
```

### 2. Configure ObjectMapper globally using Spring Boot

Create `JacksonConfig.java`:

```java
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder
            .modules(new JavaTimeModule())
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .simpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    }
}
```

📌 **DO NOT declare a separate `ObjectMapper` Bean unless you want to fully override Spring Boot's default.**

---

### 3. Always use Spring's ObjectMapper

Inject the global mapper like this:

```java
@Autowired
private ObjectMapper objectMapper;
```

❌ Do NOT use:

```java
ObjectMapper mapper = new ObjectMapper();
```

✅ Do use:

```java
ObjectMapper mapper = objectMapper; // injected from Spring
```

---

### 4. Inject ObjectMapper into external libraries (e.g., Elasticsearch)

```java
@Bean
public ElasticsearchClient elasticsearchClient(ObjectMapper objectMapper) {
    RestClient restClient = RestClient.builder(
        new HttpHost("localhost", 9200, "http")
    ).build();

    RestClientTransport transport = new RestClientTransport(
        restClient, new JacksonJsonpMapper(objectMapper)
    );

    return new ElasticsearchClient(transport);
}
```

---

## ✅ Runtime Verification

Add a test method to confirm configuration:

```java
@PostConstruct
public void testJackson() throws Exception {
    Map<String, Object> map = Map.of("now", LocalDateTime.now());
    System.out.println("✅ JSON: " + objectMapper.writeValueAsString(map));
}
```

---

## ✅ Summary

| Action                                | Result                                  |
|---------------------------------------|-----------------------------------------|
| ✅ Use `@Autowired ObjectMapper`      | Gets Spring-configured instance         |
| ✅ Use `jacksonCustomizer()`          | Extends Jackson config without override |
| ❌ Define your own `ObjectMapper` Bean| Overwrites Spring config, causes issues |
| ❌ Manually `new ObjectMapper()`      | Ignores all Spring configuration        |

---
Happy coding! 🎉

