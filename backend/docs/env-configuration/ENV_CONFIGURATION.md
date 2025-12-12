# .env Configuration Implementation

## Overview
The ProviderService uses an industry-standard approach to load environment variables from a `.env` file using Spring's native `@PropertySource` annotation with a custom `PropertySourceFactory`.

## Why This Approach?

This implementation uses **Spring's native PropertySourceFactory pattern**, which is:
- ✅ **Zero External Dependencies** - Uses only Spring Framework built-in features
- ✅ **Industry Standard** - Widely adopted pattern in enterprise Spring applications
- ✅ **Type-Safe** - Full integration with Spring's Environment and @Value annotations
- ✅ **Fail-Safe** - Gracefully handles missing .env files with `ignoreResourceNotFound = true`
- ✅ **Configurable** - Easy to extend for different property sources

## Files Involved

### 1. `.env` File (Root of ProviderService)
**Location:** `services/ProviderService/.env`

```properties
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=Provider
DB_USERNAME=postgres
DB_PASSWORD=1234

# Server Configuration
SERVER_PORT=8004

# Eureka Configuration
EUREKA_URL=http://127.0.0.1:8761/eureka/
```

### 2. EnvPropertySourceFactory.java
**Location:** `services/ProviderService/src/main/java/com/swifttrack/ProviderService/conf/EnvPropertySourceFactory.java`

Custom implementation of Spring's `PropertySourceFactory` interface that:
- Parses .env files with KEY=value format
- Handles comments (#, //)
- Supports quoted values
- Validates property format using regex patterns

### 3. EnvConfiguration.java
**Location:** `services/ProviderService/src/main/java/com/swifttrack/ProviderService/conf/EnvConfiguration.java`

Spring Configuration class that:
- Uses `@PropertySource` annotation
- Points to the custom `EnvPropertySourceFactory`
- Sets `ignoreResourceNotFound = true` for graceful degradation

### 4. application.yaml
**Location:** `services/ProviderService/src/main/resources/application.yaml`

Uses standard Spring property resolution syntax:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT:5432}/${DB_NAME:Provider}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:1234}
```

## How It Works

1. **Application Startup**
   - Spring Boot loads `ProviderServiceApplication`
   - `@Import(EnvConfiguration.class)` is processed
   
2. **Configuration Loading**
   - `EnvConfiguration` triggers `@PropertySource` 
   - `EnvPropertySourceFactory` is instantiated
   - `.env` file is located and parsed
   - Properties are added to Spring's Environment

3. **Property Resolution**
   - YAML files reference properties using `${PROPERTY_NAME}` syntax
   - Default values can be specified: `${PROPERTY_NAME:defaultValue}`
   - Spring's Environment resolves values at runtime

4. **@Value Injection**
   ```java
   @Value("${DB_HOST:localhost}")
   private String dbHost;
   ```

## Usage in Code

### Option 1: Using @Value Annotation
```java
@Component
public class MyService {
    @Value("${DB_HOST}")
    private String dbHost;
    
    @Value("${DB_PORT:5432}")
    private String dbPort;
}
```

### Option 2: Using Environment
```java
@Component
public class MyService {
    @Autowired
    private Environment env;
    
    public void printConfig() {
        String host = env.getProperty("DB_HOST");
        String port = env.getProperty("DB_PORT", "5432");
    }
}
```

## .env File Format

### Valid Syntax
```properties
# Comments are supported
KEY1=value1
KEY2="quoted value"
KEY3='single quoted value'
// C-style comments too

// Empty lines are ignored

EUREKA_URL=http://127.0.0.1:8761/eureka/
```

### Invalid Syntax (Will be skipped)
```properties
KEY without value
: value without key
  = empty key
```

## Environment Variable Priority

Properties are resolved in this order (highest to lowest priority):
1. System environment variables
2. `.env` file properties
3. `application.yaml` default values
4. Hardcoded fallback in application.yaml

**Example:**
```yaml
url: jdbc:postgresql://${DB_HOST}:${DB_PORT:5432}/${DB_NAME:Provider}
```

Will resolve as:
1. First try: OS environment variable `DB_HOST`
2. Then try: `.env` file value
3. Then try: `DB_PORT` default value (5432)

## Testing Environment Variables

Use the test endpoint to verify configuration is loaded correctly:

```bash
curl http://localhost:8004/api/env/test
```

Response:
```json
{
  "DB_HOST": "localhost",
  "DB_PORT": "5432",
  "DB_NAME": "Provider",
  "DB_USERNAME": "postgres",
  "SERVER_PORT": "8004",
  "EUREKA_URL": "http://127.0.0.1:8761/eureka/"
}
```

## Security Considerations

⚠️ **Important:** The `.env` file should be added to `.gitignore` to prevent committing sensitive data.

```gitignore
.env
.env.local
.env.*.local
```

## Adding New Environment Variables

1. Add to `.env` file:
   ```properties
   MY_NEW_VAR=my_value
   ```

2. Use in `application.yaml`:
   ```yaml
   my:
     config:
       value: ${MY_NEW_VAR:default}
   ```

3. Or inject with @Value:
   ```java
   @Value("${MY_NEW_VAR}")
   private String myNewVar;
   ```

## Troubleshooting

### Properties Not Loading
- Verify `.env` file exists in the ProviderService root directory
- Check property names match exactly (case-sensitive)
- Ensure file uses `KEY=VALUE` format

### Spring Boot Startup Issues
- If missing, check `ignoreResourceNotFound = true` is set
- Review application logs for parsing errors
- Verify no special characters in property values without quotes

## Comparison with Other Approaches

| Approach | Dependencies | Spring Integration | Industry Standard |
|----------|-------------|------------------|------------------|
| **PropertySourceFactory** | None | Native | ✅ Yes |
| dotenv-java | External | Manual | ⚠️ Moderate |
| spring-dotenv | External | Good | ⚠️ Moderate |
| OS Env Variables Only | None | Native | ✅ Yes |

## References

- [Spring @PropertySource Documentation](https://docs.spring.io/spring-framework/reference/core/beans/environment/propertysources.html)
- [Spring PropertySourceFactory](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/core/io/support/PropertySourceFactory.html)
- [12 Factor App - Environment Variables](https://12factor.net/config)
