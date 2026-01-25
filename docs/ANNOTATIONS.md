# Annotation Reference

This document provides detailed information about all annotations available in the annotation processor library.

## Table of Contents

- [@DbEntity](#dbentity) - Mark classes as database entities
- [@DbField](#dbfield) - Map fields to database columns
- [@Builder](#builder) - Generate builder pattern classes (Not Yet Implemented)

---

## @DbEntity

**Target**: Classes (TYPE)  
**Retention**: SOURCE (compile-time only)  
**Package**: `com.mohamad4444.github`

### Description

The `@DbEntity` annotation marks a class as a database entity. When applied to a class, the annotation processor generates a corresponding database access class (suffixed with `DB`) that extends `AbstractDB<T>`.

### Parameters

| Parameter   | Type   | Required | Description                                        |
| ----------- | ------ | -------- | -------------------------------------------------- |
| `tableName` | String | Yes      | The name of the database table this entity maps to |

### Usage

```java
@DbEntity(tableName = "users")
public class UserDTO {
    // fields with @DbField annotations
}
```

### Generated Code

For a class annotated with `@DbEntity`, the processor generates:

1. A new class with the name `<OriginalClassName>DB`
2. The class extends `AbstractDB<OriginalClassName>`
3. Implements the `buildDTO(ResultSet rs)` method
4. The `buildDTO` method constructs the DTO using constructor parameters mapped from ResultSet

**Example:**

Input class:
```java
@DbEntity(tableName = "categories")
public class CategoryDTO {
    @DbField(columnName = "id", isPrimaryKey = true)
    private int id;
    
    @DbField(columnName = "name")
    private String name;
}
```

Generated class:
```java
public class CategoryDTODB extends AbstractDB<CategoryDTO> {
    @Override
    protected CategoryDTO buildDTO(ResultSet rs) throws SQLException {
        return new CategoryDTO(
            rs.getInt("id"), 
            rs.getString("name")
        );
    }
}
```

### Requirements

- The annotated class must have a constructor that accepts all `@DbField` annotated fields in order
- All fields you want mapped must be annotated with `@DbField`
- The class should be a proper DTO/POJO with getters/setters (or use Lombok)

### Best Practices

1. Use descriptive table names that match your database schema
2. Keep DTOs focused on a single table
3. Use the `@Data`, `@NoArgsConstructor`, and `@AllArgsConstructor` Lombok annotations for cleaner code
4. Order fields in the class to match the constructor parameter order

---

## @DbField

**Target**: Fields (FIELD)  
**Retention**: SOURCE (compile-time only)  
**Package**: `com.mohamad4444.github`

### Description

The `@DbField` annotation marks a field for database column mapping. Fields annotated with `@DbField` are included in the generated `buildDTO` method with appropriate ResultSet getter methods based on the field type.

### Parameters

| Parameter      | Type    | Required | Default | Description                                          |
| -------------- | ------- | -------- | ------- | ---------------------------------------------------- |
| `columnName`   | String  | Yes      | -       | The name of the database column to map this field to |
| `isPrimaryKey` | boolean | No       | `false` | Indicates if this field represents the primary key   |

### Usage

```java
@DbField(columnName = "user_id", isPrimaryKey = true)
private int userId;

@DbField(columnName = "email")
private String email;
```

### Type Mapping

The annotation processor automatically selects the appropriate ResultSet getter method based on the field's Java type:

| Java Type            | ResultSet Method           | Example                                                            |
| -------------------- | -------------------------- | ------------------------------------------------------------------ |
| `int`                | `getInt(columnName)`       | `@DbField(columnName = "age") private int age;`                    |
| `long`               | `getLong(columnName)`      | `@DbField(columnName = "count") private long count;`               |
| `float`              | `getFloat(columnName)`     | `@DbField(columnName = "rate") private float rate;`                |
| `double`             | `getDouble(columnName)`    | `@DbField(columnName = "price") private double price;`             |
| `boolean`            | `getBoolean(columnName)`   | `@DbField(columnName = "active") private boolean active;`          |
| `java.lang.String`   | `getString(columnName)`    | `@DbField(columnName = "name") private String name;`               |
| `java.sql.Date`      | `getDate(columnName)`      | `@DbField(columnName = "birth_date") private Date birthDate;`      |
| `java.sql.Timestamp` | `getTimestamp(columnName)` | `@DbField(columnName = "created_at") private Timestamp createdAt;` |
| Other types          | `getObject(columnName)`    | `@DbField(columnName = "data") private CustomType data;`           |

### Requirements

- Must be applied to instance fields (not static or final)
- The field must be part of a class annotated with `@DbEntity`
- Field ordering matters - they should match the constructor parameter order

### Best Practices

1. Use snake_case column names to match database conventions
2. Mark primary key fields with `isPrimaryKey = true` for clarity
3. Use exact column names from your database schema
4. Keep field types compatible with JDBC types

### Example

Complete example with multiple field types:

```java
@DbEntity(tableName = "orders")
public class OrderDTO {
    
    @DbField(columnName = "order_id", isPrimaryKey = true)
    private long orderId;
    
    @DbField(columnName = "customer_name")
    private String customerName;
    
    @DbField(columnName = "order_date")
    private java.sql.Date orderDate;
    
    @DbField(columnName = "total_amount")
    private double totalAmount;
    
    @DbField(columnName = "is_shipped")
    private boolean isShipped;
}
```

---

## @Builder

**Target**: Classes (TYPE)  
**Retention**: SOURCE (compile-time only)  
**Package**: `com.mohamad4444.github`

### Description

The `@Builder` annotation is designed to generate builder pattern classes for the annotated class. 

> **⚠️ NOT YET IMPLEMENTED**: This annotation is currently defined but not implemented. The processor does not generate builder classes yet.

### Planned Usage

```java
@Builder
public class Person {
    private String name;
    private int age;
    private String email;
}
```

### Planned Generated Code

When implemented, this would generate:

```java
public class PersonBuilder {
    private String name;
    private int age;
    private String email;
    
    public PersonBuilder name(String value) {
        name = value;
        return this;
    }
    
    public PersonBuilder age(int value) {
        age = value;
        return this;
    }
    
    public PersonBuilder email(String value) {
        email = value;
        return this;
    }
    
    public Person build() {
        return new Person(name, age, email);
    }
}
```

### Future Plans

- Implement the builder pattern code generation
- Support custom builder method names
- Support optional fields
- Integration with validation

---

## Common Patterns

### Combining with Lombok

The annotations work well with Lombok annotations:

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@DbEntity(tableName = "products")
public class ProductDTO {
    
    @DbField(columnName = "product_id", isPrimaryKey = true)
    private int productId;
    
    @DbField(columnName = "product_name")
    private String productName;
    
    @DbField(columnName = "price")
    private double price;
}
```

### Using Generated Classes

```java
// In your data access layer
ProductDTODB productDb = new ProductDTODB();

// Query all products
List<ProductDTO> products = productDb.executeQuery(
    connection, 
    "SELECT * FROM products"
);

// Query single product
ProductDTO product = productDb.executeSingleQuery(
    connection, 
    "SELECT * FROM products WHERE product_id = ?", 
    productId
);

// Update/Insert
int rowsAffected = productDb.executeUpdate(
    connection,
    "INSERT INTO products (product_name, price) VALUES (?, ?)",
    "New Product",
    29.99
);
```

## Limitations

1. **Constructor Requirement**: Classes with `@DbEntity` must have a constructor that accepts all `@DbField` annotated fields
2. **Field Order**: Field order in the class must match constructor parameter order
3. **Type Support**: Only common JDBC types are automatically mapped; custom types will use `getObject()`
4. **No Inheritance**: Generated classes don't support inherited fields (only direct fields are processed)

## Extending the Processor

To add support for new types or customize the generated code, see the [CONTRIBUTING.md](../CONTRIBUTING.md) guide.
