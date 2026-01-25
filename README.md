# Java Annotation Processor

A powerful Java annotation processing library that generates boilerplate code at compile-time, reducing repetitive tasks in your codebase. This project demonstrates how annotation processors work in Java/Spring Boot applications and provides practical implementations for database entity mapping and the builder pattern.

## 🎯 Purpose

This project was created to:
- **Deep dive into annotation processing** - Understand how Java annotation processors work under the hood
- **Automate repetitive tasks** - Eliminate boilerplate code for database operations and object construction
- **Explore Spring Boot integration** - Test the limits of compile-time code generation in Spring Boot applications
- **Provide a foundation** - Serve as a starting point for custom annotation processors in your projects

## ✨ Features

### Database Entity Generation (`@DbEntity` / `@DbField`)

Automatically generates database access classes that extend `AbstractDB<T>` with:
- Type-safe ResultSet mapping
- Automatic field-to-column mapping
- Support for primitive and common Java types (String, Date, Timestamp, etc.)
- Primary key field marking

### Builder Pattern (Planned)

The `@Builder` annotation is defined but not yet implemented. This will generate builder classes following the builder pattern.

## 🏗️ Architecture

The project uses a multi-module Maven structure:

```
annotation-processing/           # Parent module
├── processor/                   # Annotation definitions and processing logic
│   └── src/main/java/
│       ├── AbstractDB.java      # Base class for generated DB entities
│       ├── BuilderProcessor.java # Main annotation processor
│       ├── DbEntity.java        # Class-level annotation
│       ├── DbField.java         # Field-level annotation
│       └── Builder.java         # (Future) Builder pattern annotation
└── model/                       # Example models using the annotations
    └── src/main/java/
        ├── CategoryDTO.java     # Example entity with @DbEntity
        └── Employee.java        # Example with @Builder (not yet functional)
```

### How It Works

1. **Compile-time Processing**: When you compile the `model` module, the Java compiler invokes `BuilderProcessor`
2. **Annotation Scanning**: The processor scans for classes annotated with `@DbEntity`
3. **Code Generation**: For each annotated class, it generates a corresponding `*DB` class
4. **Type Mapping**: Fields with `@DbField` are mapped to appropriate ResultSet getter methods based on their types
5. **Output**: Generated classes are written to `target/generated-sources/annotations/`

## 🚀 Quick Start

### Prerequisites

- Java 11 or higher
- Maven 3.6+

### Building the Project

```bash
# Clone the repository
git clone <your-repo-url>
cd AnnotationProcessor

# Build the entire project
mvn clean install
```

This will:
1. Compile the `processor` module with annotation definitions
2. Compile the `model` module, triggering annotation processing
3. Generate database entity classes in `model/target/generated-sources/annotations/`

### Using the Annotations

#### Example: Database Entity

```java
package com.example;

import com.mohamad4444.github.DbEntity;
import com.mohamad4444.github.DbField;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DbEntity(tableName = "users")
public class UserDTO {
    
    @DbField(columnName = "id", isPrimaryKey = true)
    private int id;
    
    @DbField(columnName = "username")
    private String username;
    
    @DbField(columnName = "email")
    private String email;
    
    @DbField(columnName = "created_at")
    private java.sql.Timestamp createdAt;
}
```

#### Generated Code

After compilation, this generates `UserDTODB.java`:

```java
package com.example;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDTODB extends AbstractDB<UserDTO> {

    @Override
    protected UserDTO buildDTO(ResultSet rs) throws SQLException {
        return new UserDTO(
            rs.getInt("id"), 
            rs.getString("username"), 
            rs.getString("email"), 
            rs.getTimestamp("created_at")
        );
    }
}
```

You can then use this generated class in your data access layer:

```java
UserDTODB userDb = new UserDTODB();
List<UserDTO> users = userDb.executeQuery(connection, "SELECT * FROM users");
UserDTO user = userDb.executeSingleQuery(connection, "SELECT * FROM users WHERE id = ?", userId);
```

## 📚 Documentation

- **[Annotation Reference](docs/ANNOTATIONS.md)** - Detailed documentation for all annotations
- **[Examples](docs/EXAMPLES.md)** - Practical usage examples and patterns
- **[Contributing Guide](CONTRIBUTING.md)** - How to extend and modify the processor

## 🔧 Supported Types

The processor automatically maps Java types to appropriate ResultSet methods:

| Java Type            | ResultSet Method |
| -------------------- | ---------------- |
| `int`                | `getInt()`       |
| `long`               | `getLong()`      |
| `float`              | `getFloat()`     |
| `double`             | `getDouble()`    |
| `boolean`            | `getBoolean()`   |
| `java.lang.String`   | `getString()`    |
| `java.sql.Date`      | `getDate()`      |
| `java.sql.Timestamp` | `getTimestamp()` |
| Other types          | `getObject()`    |

## 🛠️ Development

### Project Structure

- **processor/** - Contains annotation definitions and the annotation processor
  - Uses Google's AutoService for automatic processor registration
  - Generates code using JavaPoet-style string building
  
- **model/** - Example module that uses the annotations
  - Configured with annotation processor path in pom.xml
  - Generated sources appear in `target/generated-sources/annotations/`

### Adding New Types

To support additional types, modify the `getResultSetMethod()` in `BuilderProcessor.java`:

```java
private String getResultSetMethod(TypeMirror fieldType) {
    // Add your custom type mapping here
    if (fieldType.toString().equals("java.math.BigDecimal")) {
        return "getBigDecimal";
    }
    // ... existing mappings ...
}
```

## 🤝 Contributing

Contributions are welcome! See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on:
- Adding new annotations
- Extending the processor
- Testing your changes
- Code style guidelines

## 📝 License

This project is provided as-is for educational and practical use. Feel free to use, modify, and distribute as needed.

## 🙏 Acknowledgments

This project was inspired by exploring annotation processing capabilities in Java and understanding how frameworks like Lombok and JPA work behind the scenes.

## 📧 Contact

For questions or suggestions, please open an issue in the repository.

---

**Note**: The `@Builder` annotation is currently a placeholder for future implementation. The primary focus is on the database entity generation functionality.
