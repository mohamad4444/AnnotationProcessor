# Usage Examples

This guide provides practical examples of using the annotation processor in real-world scenarios.

## Table of Contents

- [Basic Example](#basic-example)
- [Complete CRUD Example](#complete-crud-example)
- [Multiple Tables](#multiple-tables)
- [Complex Types](#complex-types)
- [Integration with Spring Boot](#integration-with-spring-boot)

---

## Basic Example

Let's start with a simple user table.

### Step 1: Define Your Entity

```java
package com.example.model;

import com.mohamad4444.github.DbEntity;
import com.mohamad4444.github.DbField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

### Step 2: Build the Project

```bash
mvn clean install
```

### Step 3: Check Generated Code

After building, check `target/generated-sources/annotations/com/example/model/UserDTODB.java`:

```java
package com.example.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import com.example.model.UserDTO;

public class UserDTODB extends AbstractDB<UserDTO> {

    @Override
    protected UserDTO buildDTO(ResultSet rs) throws SQLException {
        return new UserDTO(
            rs.getInt("id"), 
            rs.getString("username"), 
            rs.getString("email")
        );
    }
}
```

### Step 4: Use in Your Code

```java
public class UserRepository {
    private final UserDTODB userDb = new UserDTODB();
    
    public List<UserDTO> findAll(Connection connection) {
        return userDb.executeQuery(
            connection, 
            "SELECT * FROM users"
        );
    }
    
    public UserDTO findById(Connection connection, int userId) {
        return userDb.executeSingleQuery(
            connection,
            "SELECT * FROM users WHERE id = ?",
            userId
        );
    }
}
```

---

## Complete CRUD Example

A full example with Create, Read, Update, Delete operations.

### Entity Definition

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
    
    @DbField(columnName = "description")
    private String description;
    
    @DbField(columnName = "price")
    private double price;
    
    @DbField(columnName = "in_stock")
    private boolean inStock;
    
    @DbField(columnName = "created_at")
    private java.sql.Timestamp createdAt;
}
```

### Repository Implementation

```java
public class ProductRepository {
    private final ProductDTODB productDb = new ProductDTODB();
    
    // CREATE
    public int createProduct(Connection conn, String name, String desc, 
                            double price, boolean inStock) {
        String sql = "INSERT INTO products (product_name, description, price, " +
                    "in_stock, created_at) VALUES (?, ?, ?, ?, NOW())";
        return productDb.executeUpdate(conn, sql, name, desc, price, inStock);
    }
    
    // READ - All
    public List<ProductDTO> getAllProducts(Connection conn) {
        return productDb.executeQuery(conn, "SELECT * FROM products");
    }
    
    // READ - By ID
    public ProductDTO getProductById(Connection conn, int productId) {
        return productDb.executeSingleQuery(
            conn,
            "SELECT * FROM products WHERE product_id = ?",
            productId
        );
    }
    
    // READ - With Filter
    public List<ProductDTO> getInStockProducts(Connection conn) {
        return productDb.executeQuery(
            conn,
            "SELECT * FROM products WHERE in_stock = true"
        );
    }
    
    // UPDATE
    public int updatePrice(Connection conn, int productId, double newPrice) {
        return productDb.executeUpdate(
            conn,
            "UPDATE products SET price = ? WHERE product_id = ?",
            newPrice,
            productId
        );
    }
    
    // DELETE
    public int deleteProduct(Connection conn, int productId) {
        return productDb.executeUpdate(
            conn,
            "DELETE FROM products WHERE product_id = ?",
            productId
        );
    }
}
```

### Usage Example

```java
public class ProductService {
    private ProductRepository repository = new ProductRepository();
    
    public void demonstrateUsage() {
        try (Connection conn = getConnection()) {
            // Create a new product
            repository.createProduct(
                conn,
                "Laptop",
                "High-performance laptop",
                1299.99,
                true
            );
            
            // Get all products
            List<ProductDTO> allProducts = repository.getAllProducts(conn);
            allProducts.forEach(p -> System.out.println(p.getProductName()));
            
            // Get specific product
            ProductDTO laptop = repository.getProductById(conn, 1);
            System.out.println("Price: $" + laptop.getPrice());
            
            // Update price
            repository.updatePrice(conn, 1, 1199.99);
            
            // Get filtered products
            List<ProductDTO> available = repository.getInStockProducts(conn);
            System.out.println("Available products: " + available.size());
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private Connection getConnection() throws SQLException {
        // Your database connection logic
        return null;
    }
}
```

---

## Multiple Tables

Working with multiple related entities.

### Category Entity

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@DbEntity(tableName = "categories")
public class CategoryDTO {
    
    @DbField(columnName = "category_id", isPrimaryKey = true)
    private int categoryId;
    
    @DbField(columnName = "category_name")
    private String categoryName;
    
    @DbField(columnName = "description")
    private String description;
}
```

### Product Entity (with category reference)

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
    
    @DbField(columnName = "category_id")
    private int categoryId;  // Foreign key
    
    @DbField(columnName = "price")
    private double price;
}
```

### Service Layer with Joins

```java
public class ProductService {
    private final ProductDTODB productDb = new ProductDTODB();
    private final CategoryDTODB categoryDb = new CategoryDTODB();
    
    public List<ProductDTO> getProductsByCategory(Connection conn, int categoryId) {
        return productDb.executeQuery(
            conn,
            "SELECT * FROM products WHERE category_id = ?",
            categoryId
        );
    }
    
    public Map<CategoryDTO, List<ProductDTO>> getAllProductsGroupedByCategory(
            Connection conn) {
        
        Map<CategoryDTO, List<ProductDTO>> result = new HashMap<>();
        
        List<CategoryDTO> categories = categoryDb.executeQuery(
            conn, 
            "SELECT * FROM categories"
        );
        
        for (CategoryDTO category : categories) {
            List<ProductDTO> products = getProductsByCategory(
                conn, 
                category.getCategoryId()
            );
            result.put(category, products);
        }
        
        return result;
    }
}
```

---

## Complex Types

Working with various data types.

### Entity with Multiple Types

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@DbEntity(tableName = "orders")
public class OrderDTO {
    
    @DbField(columnName = "order_id", isPrimaryKey = true)
    private long orderId;                    // long
    
    @DbField(columnName = "customer_name")
    private String customerName;             // String
    
    @DbField(columnName = "order_date")
    private java.sql.Date orderDate;        // Date
    
    @DbField(columnName = "total_amount")
    private double totalAmount;              // double
    
    @DbField(columnName = "discount_rate")
    private float discountRate;              // float
    
    @DbField(columnName = "is_shipped")
    private boolean isShipped;               // boolean
    
    @DbField(columnName = "created_at")
    private java.sql.Timestamp createdAt;   // Timestamp
    
    @DbField(columnName = "quantity")
    private int quantity;                    // int
}
```

### Generated buildDTO Method

```java
@Override
protected OrderDTO buildDTO(ResultSet rs) throws SQLException {
    return new OrderDTO(
        rs.getLong("order_id"),
        rs.getString("customer_name"),
        rs.getDate("order_date"),
        rs.getDouble("total_amount"),
        rs.getFloat("discount_rate"),
        rs.getBoolean("is_shipped"),
        rs.getTimestamp("created_at"),
        rs.getInt("quantity")
    );
}
```

---

## Integration with Spring Boot

Using the annotation processor in a Spring Boot application.

### Step 1: Add Dependencies (pom.xml)

```xml
<dependencies>
    <!-- Your annotation processor -->
    <dependency>
        <groupId>com.mohamad4444.github</groupId>
        <artifactId>processor</artifactId>
        <version>1.0-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
    
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jdbc</artifactId>
    </dependency>
    
    <!-- Database driver -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
    </dependency>
</dependencies>
```

### Step 2: Configure Annotation Processing

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.13.0</version>
            <configuration>
                <annotationProcessorPaths>
                    <path>
                        <groupId>com.mohamad4444.github</groupId>
                        <artifactId>processor</artifactId>
                        <version>1.0-SNAPSHOT</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### Step 3: Create Repository with Spring

```java
@Repository
public class UserRepository {
    
    private final UserDTODB userDb = new UserDTODB();
    
    @Autowired
    private DataSource dataSource;
    
    public List<UserDTO> findAll() {
        try (Connection conn = dataSource.getConnection()) {
            return userDb.executeQuery(conn, "SELECT * FROM users");
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching users", e);
        }
    }
    
    public UserDTO findById(int userId) {
        try (Connection conn = dataSource.getConnection()) {
            return userDb.executeSingleQuery(
                conn,
                "SELECT * FROM users WHERE id = ?",
                userId
            );
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching user", e);
        }
    }
    
    @Transactional
    public void createUser(String username, String email) {
        try (Connection conn = dataSource.getConnection()) {
            userDb.executeUpdate(
                conn,
                "INSERT INTO users (username, email) VALUES (?, ?)",
                username, email
            );
        } catch (SQLException e) {
            throw new RuntimeException("Error creating user", e);
        }
    }
}
```

### Step 4: Use in Service Layer

```java
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll();
    }
    
    public UserDTO getUserById(int userId) {
        UserDTO user = userRepository.findById(userId);
        if (user == null) {
            throw new EntityNotFoundException("User not found");
        }
        return user;
    }
    
    public void registerUser(String username, String email) {
        // Business logic validation
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username required");
        }
        userRepository.createUser(username, email);
    }
}
```

### Step 5: REST Controller

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable int id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
    
    @PostMapping
    public ResponseEntity<Void> createUser(@RequestBody CreateUserRequest request) {
        userService.registerUser(request.getUsername(), request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
```

---

## Best Practices

1. **Keep DTOs Simple**: Focus on data mapping, not business logic
2. **Use Lombok**: Reduce boilerplate with `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`
3. **Connection Management**: Always use try-with-resources for connections
4. **Error Handling**: Wrap SQL exceptions appropriately for your layer
5. **Naming Conventions**: Use clear, descriptive names for tables and columns
6. **Field Order**: Ensure constructor parameter order matches field order
7. **Transaction Management**: Use Spring's `@Transactional` for complex operations

---

## Troubleshooting

### Generated class not found

**Problem**: `CategoryDTODB` class not found.

**Solution**: 
1. Run `mvn clean install` to trigger annotation processing
2. Check `target/generated-sources/annotations/` for the generated class
3. Ensure your IDE is configured to recognize generated sources

### Constructor mismatch error

**Problem**: Generated code has wrong constructor call.

**Solution**: Ensure your DTO has a constructor with all `@DbField` fields in the same order they appear in the class.

### Wrong ResultSet method

**Problem**: `ClassCastException` or type mismatch at runtime.

**Solution**: Check that your Java field types match the database column types. The processor uses type mapping - review the [Type Mapping table](ANNOTATIONS.md#type-mapping).

---

For more information, see:
- [Annotation Reference](ANNOTATIONS.md)
- [Contributing Guide](../CONTRIBUTING.md)
