package com.mohamad4444.github;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field for database column mapping in generated code.
 * 
 * <p>
 * This annotation is used on fields within a {@link DbEntity} annotated class
 * to specify which database column the field maps to. The annotation processor
 * automatically selects the appropriate ResultSet getter method based on the
 * field's Java type.
 * 
 * <p>
 * <b>Supported Type Mappings:</b>
 * <ul>
 * <li>{@code int} → {@code ResultSet.getInt()}</li>
 * <li>{@code long} → {@code ResultSet.getLong()}</li>
 * <li>{@code float} → {@code ResultSet.getFloat()}</li>
 * <li>{@code double} → {@code ResultSet.getDouble()}</li>
 * <li>{@code boolean} → {@code ResultSet.getBoolean()}</li>
 * <li>{@code String} → {@code ResultSet.getString()}</li>
 * <li>{@code java.sql.Date} → {@code ResultSet.getDate()}</li>
 * <li>{@code java.sql.Timestamp} → {@code ResultSet.getTimestamp()}</li>
 * <li>Other types → {@code ResultSet.getObject()}</li>
 * </ul>
 * 
 * <p>
 * <b>Example:</b>
 * 
 * <pre>
 * {
 *     &#64;code
 *     &#64;DbEntity(tableName = "products")
 *     public class ProductDTO {
 *         &#64;DbField(columnName = "product_id", isPrimaryKey = true)
 *         private int productId;
 * 
 *         &#64;DbField(columnName = "product_name")
 *         private String productName;
 * 
 *         @DbField(columnName = "price")
 *         private double price;
 *     }
 * }
 * </pre>
 * 
 * @see DbEntity
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface DbField {
    /**
     * The name of the database column this field maps to.
     * 
     * @return the column name
     */
    String columnName();

    /**
     * Indicates whether this field represents the primary key.
     * 
     * <p>
     * This is primarily used for documentation and clarity. The generated
     * code does not currently treat primary key fields differently, but this
     * may be used for future enhancements.
     * 
     * @return {@code true} if this is the primary key field, {@code false}
     *         otherwise
     */
    boolean isPrimaryKey() default false;
}