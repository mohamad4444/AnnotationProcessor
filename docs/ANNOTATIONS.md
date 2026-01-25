# Annotation Reference

Complete reference for the annotation processor annotations.

## @DbEntity

**Target:** Classes  
**Purpose:** Marks a class as a database entity for code generation

### Parameters

- `tableName` (String, required) - The database table name

### Example

```java
@DbEntity(tableName = "users")
public class UserDTO {
    // fields with @DbField
}
```

### Generated Code

Creates a class `<OriginalClass>DB` extending `AbstractDB<OriginalClass>` in `target/generated-sources/annotations/`:

```java
public class UserDTODB extends AbstractDB<UserDTO> {
    @Override
    protected UserDTO buildDTO(ResultSet rs) throws SQLException {
        return new UserDTO(
            rs.getInt("id"),
            rs.getString("username")
        );
    }
}
```

### Requirements

- Class must have a constructor accepting all `@DbField` annotated fields
- Field order must match constructor parameter order

---

## @DbField

**Target:** Fields  
**Purpose:** Maps a field to a database column

### Parameters

- `columnName` (String, required) - The database column name
- `isPrimaryKey` (boolean, optional, default=false) - Marks primary key field

### Example

```java
@DbField(columnName = "user_id", isPrimaryKey = true)
private int userId;

@DbField(columnName = "email")
private String email;
```

### Type Mappings

The processor automatically selects ResultSet methods based on field type:

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
| Other                | `getObject()`    |

---

## @Builder

**Status:** Not implemented (placeholder for future)

This annotation is defined but doesn't generate code yet.

---

## Complete Example

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
    
    @DbField(columnName = "created_at")
    private java.sql.Timestamp createdAt;
}
```

**Generated:** `ProductDTODB.java`

**Usage:**
```java
ProductDTODB productDb = new ProductDTODB();
List<ProductDTO> products = productDb.executeQuery(
    connection, 
    "SELECT * FROM products WHERE price > ?",
    100.0
);
```

---

## Extending the Processor

To add support for new types, modify `getResultSetMethod()` in `BuilderProcessor.java`:

```java
private String getResultSetMethod(TypeMirror fieldType) {
    // Add custom type mapping
    if (fieldType.toString().equals("java.math.BigDecimal")) {
        return "getBigDecimal";
    }
    // ... existing mappings
}
```
