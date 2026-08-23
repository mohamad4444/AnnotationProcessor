# Java Annotation Processor

## 🤖 AI Usage Disclosure
This project is **AI-Assisted**. The core logic for the annotation processor was written manually in September 2024. AI tools were introduced in January 2026 (starting from commit `cc43b3d`) exclusively to generate JavaDocs, simplify the project documentation (`README.md`), and update project metadata.

A simple Java annotation processor that generates database entity classes at compile-time, reducing boilerplate code for database operations. 

This project was made to understand how annotation processors work and how to use them, especially to deeply understand Spring Boot's JPA annotations. and make it possible to generate our custom annotations for database entities. 

## Features

- **@DbEntity** - Generates database access classes from annotated DTOs
- **@DbField** - Automatic field-to-column mapping with type-safe ResultSet handling
- Supports primitive types and common Java/SQL types (String, Date, Timestamp)

## Quick Start

### 1. Define Your Entity

```java
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
}
```

### 2. Build the Project

```bash
mvn clean install
```

### 3. Use the Generated Class

The processor generates `UserDTODB.java` in `target/generated-sources/annotations/`:

```java
UserDTODB userDb = new UserDTODB();
List<UserDTO> users = userDb.executeQuery(
    connection, 
    "SELECT * FROM users"
);
```

## How It Works

The annotation processor:
1. Scans for classes with `@DbEntity` at compile-time
2. Processes fields annotated with `@DbField`  
3. Generates a class extending `AbstractDB<T>` with a `buildDTO(ResultSet)` method
4. Maps Java types to appropriate ResultSet getters (int→getInt, String→getString, etc.)

## Project Structure

```
AnnotationProcessor/
├── processor/          # Annotation definitions and processor logic
├── model/             # Example usage (CategoryDTO, Employee)
└── docs/              # API reference
```

## Type Mappings

| Java Type | ResultSet Method |
| --------- | ---------------- |
| int       | getInt()         |
| long      | getLong()        |
| String    | getString()      |
| boolean   | getBoolean()     |
| double    | getDouble()      |
| Date      | getDate()        |
| Timestamp | getTimestamp()   |

For detailed annotation reference, see [docs/ANNOTATIONS.md](docs/ANNOTATIONS.md).

## Requirements

- Java 11+
- Maven 3.6+
