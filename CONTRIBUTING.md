# Contributing Guide

Thank you for your interest in contributing to the Java Annotation Processor project! This guide will help you understand how to extend and modify the annotation processor.

## Table of Contents

- [Development Setup](#development-setup)
- [Project Structure](#project-structure)
- [Adding New Annotations](#adding-new-annotations)
- [Modifying the Processor](#modifying-the-processor)
- [Testing Your Changes](#testing-your-changes)
- [Code Style Guidelines](#code-style-guidelines)

---

## Development Setup

### Prerequisites

- JDK 11 or higher
- Maven 3.6+
- An IDE (IntelliJ IDEA, Eclipse, or VS Code recommended)

### Building from Source

```bash
# Clone the repository
git clone <repository-url>
cd AnnotationProcessor

# Build the project
mvn clean install
```

### IDE Setup

**IntelliJ IDEA:**
1. Open the project as a Maven project
2. Enable annotation processing: Settings → Build, Execution, Deployment → Compiler → Annotation Processors
3. Mark `target/generated-sources/annotations` as a source folder

**Eclipse:**
1. Import as Maven project
2. Project → Properties → Java Compiler → Annotation Processing → Enable annotation processing
3. Configure generated source directory

---

## Project Structure

```
annotation-processing/
├── processor/                          # Annotation processor module
│   └── src/main/java/com/mohamad4444/github/
│       ├── AbstractDB.java             # Base class for generated DB classes
│       ├── BuilderProcessor.java       # Main annotation processor
│       ├── DbEntity.java               # Entity annotation
│       ├── DbField.java                # Field annotation
│       └── Builder.java                # Builder annotation (not implemented)
│
└── model/                              # Example usage module
    └── src/main/java/com/mohamad4444/github/
        ├── CategoryDTO.java            # Example entity
        └── Employee.java               # Example for Builder pattern
```

**Key Files:**
- `BuilderProcessor.java` - Core annotation processing logic
- `AbstractDB.java` - Base class that generated classes extend
- Annotation definitions (`@DbEntity`, `@DbField`, etc.)

---

## Adding New Annotations

### Step 1: Define the Annotation

Create a new annotation interface in the `processor` module:

```java
package com.mohamad4444.github;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)  // or ElementType.TYPE, METHOD, etc.
@Retention(RetentionPolicy.SOURCE)
public @interface YourAnnotation {
    String value() default "";
    // Add other parameters as needed
}
```

### Step 2: Register the Annotation

Update `BuilderProcessor.java` to process your annotation:

```java
@SupportedAnnotationTypes({
    "com.mohamad4444.github.DbEntity",
    "com.mohamad4444.github.DbField",
    "com.mohamad4444.github.Builder",
    "com.mohamad4444.github.YourAnnotation"  // Add your annotation
})
public class BuilderProcessor extends AbstractProcessor {
    // ...
}
```

### Step 3: Implement Processing Logic

Add processing logic in the `process()` method:

```java
@Override
public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    // Process your new annotation
    for (Element element : roundEnv.getElementsAnnotatedWith(YourAnnotation.class)) {
        // Your processing logic here
        processYourAnnotation(element);
    }
    
    // ... existing processing logic ...
    return true;
}

private void processYourAnnotation(Element element) {
    // Implement your annotation processing logic
}
```

---

## Modifying the Processor

### Adding Support for New Types

To support additional Java/SQL types, modify the `getResultSetMethod()` in `BuilderProcessor.java`:

```java
private String getResultSetMethod(TypeMirror fieldType) {
    // ... existing mappings ...
    
    // Add your custom type mapping
    if (fieldType.toString().equals("java.math.BigDecimal")) {
        return "getBigDecimal";
    } else if (fieldType.toString().equals("java.time.LocalDate")) {
        return "getObject";  // Will need casting
    }
    
    return "getObject";  // Default fallback
}
```

### Customizing Generated Code

The code generation happens in `generateDbClass()`. To customize the output:

```java
private void generateDbClass(TypeElement classElement, String tableName) {
    String className = classElement.getSimpleName() + "DB";
    String packageName = processingEnv.getElementUtils()
        .getPackageOf(classElement).toString();
    
    try {
        JavaFileObject builderFile = processingEnv.getFiler()
            .createSourceFile(packageName + "." + className);
        
        try (Writer writer = builderFile.openWriter()) {
            StringBuilder sb = new StringBuilder();
            
            // Customize the generated code here
            sb.append("package ").append(packageName).append(";\n\n");
            
            // Add your custom imports, methods, etc.
            
            writer.write(sb.toString());
        }
    } catch (Exception e) {
        processingEnv.getMessager().printMessage(
            Diagnostic.Kind.ERROR,
            "Error generating class: " + e.getMessage()
        );
    }
}
```

### Error Handling and Messaging

Use the `Messager` to report errors, warnings, or notes during processing:

```java
Messager messager = processingEnv.getMessager();

// Error - prevents compilation
messager.printMessage(Diagnostic.Kind.ERROR, 
    "Missing required annotation parameter", 
    element);

// Warning - compilation proceeds
messager.printMessage(Diagnostic.Kind.WARNING, 
    "Deprecated annotation usage", 
    element);

// Note - informational
messager.printMessage(Diagnostic.Kind.NOTE, 
    "Generating class: " + className);
```

---

## Testing Your Changes

### Unit Testing the Processor

Unfortunately, testing annotation processors is complex. Here's a recommended approach:

1. **Create test models** in the `model` module
2. **Build and inspect generated sources** manually
3. **Use compilation testing libraries** (advanced):

```xml
<dependency>
    <groupId>com.google.testing.compile</groupId>
    <artifactId>compile-testing</artifactId>
    <version>0.21.0</version>
    <scope>test</scope>
</dependency>
```

### Manual Testing Workflow

1. **Make changes** to the processor code
2. **Install the processor** module:
   ```bash
   cd processor
   mvn clean install
   ```
3. **Test in the model** module:
   ```bash
   cd ../model
   mvn clean compile
   ```
4. **Check generated sources**:
   ```bash
   ls target/generated-sources/annotations/
   ```
5. **Review the generated** code for correctness

### Creating Test Cases

Add test entities in the `model` module:

```java
@DbEntity(tableName = "test_table")
public class TestDTO {
    @DbField(columnName = "id", isPrimaryKey = true)
    private int id;
    
    @DbField(columnName = "new_type_field")
    private YourNewType newTypeField;  // Test your new type support
    
    public TestDTO(int id, YourNewType newTypeField) {
        this.id = id;
        this.newTypeField = newTypeField;
    }
}
```

Then verify the generated code handles it correctly.

---

## Code Style Guidelines

### Java Code Style

- **Indentation**: 4 spaces (no tabs)
- **Line length**: Max 120 characters
- **Naming**:
  - Classes: `PascalCase`
  - Methods/variables: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`
- **Documentation**: Add JavaDoc for all public classes and methods

### JavaDoc Standards

```java
/**
 * Processes database entity annotations and generates corresponding DB classes.
 * 
 * <p>This processor scans for {@link DbEntity} and {@link DbField} annotations
 * and generates classes that extend {@link AbstractDB} with proper ResultSet
 * mapping logic.
 *
 * @see DbEntity
 * @see DbField
 * @see AbstractDB
 */
@AutoService(Processor.class)
public class BuilderProcessor extends AbstractProcessor {
    
    /**
     * Processes annotations and generates source code.
     *
     * @param annotations the annotation interfaces requested to be processed
     * @param roundEnv environment for information about the current round
     * @return {@code true} if the annotations are claimed by this processor
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, 
                          RoundEnvironment roundEnv) {
        // Implementation
    }
}
```

### Commit Messages

Follow conventional commit format:

```
feat: add support for BigDecimal type mapping
fix: correct constructor parameter ordering
docs: update README with new examples
refactor: extract type mapping to separate method
test: add test case for LocalDate support
```

### Pull Request Guidelines

1. **Create a feature branch**: `git checkout -b feature/your-feature`
2. **Make your changes** with clear, focused commits
3. **Test thoroughly** (see Testing section)
4. **Update documentation** if adding features
5. **Submit PR** with description of changes and why they're needed

---

## Common Extension Scenarios

### Scenario 1: Adding Query Method Generation

Extend `generateDbClass()` to add finder methods:

```java
// After generating buildDTO method, add:
sb.append("    public List<" + classElement.getSimpleName() + "> findAll(Connection cn) {\n");
sb.append("        return executeQuery(cn, \"SELECT * FROM " + tableName + "\");\n");
sb.append("    }\n\n");
```

### Scenario 2: Supporting Nullable Fields

Add a `nullable` parameter to `@DbField`:

```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface DbField {
    String columnName();
    boolean isPrimaryKey() default false;
    boolean nullable() default true;  // New parameter
}
```

Then use it in the processor:

```java
DbField dbField = enclosed.getAnnotation(DbField.class);
if (!dbField.nullable()) {
    // Add null check in generated code
    sb.append("        if (rs.wasNull()) throw new SQLException(\"Required field is null\");\n");
}
```

### Scenario 3: Implementing the Builder Pattern

To implement `@Builder`, create a new method:

```java
private void generateBuilderClass(TypeElement classElement) {
    String className = classElement.getSimpleName() + "Builder";
    String packageName = processingEnv.getElementUtils()
        .getPackageOf(classElement).toString();
    
    // Generate builder pattern code
    // See BuilderProcessor2.java comments for reference implementation
}
```

---

## Getting Help

- **Issues**: Report bugs or suggest features via GitHub Issues
- **Discussions**: Ask questions in GitHub Discussions
- **Documentation**: Check existing docs in `/docs` folder

---

## License

By contributing, you agree that your contributions will be licensed under the same license as the project.
