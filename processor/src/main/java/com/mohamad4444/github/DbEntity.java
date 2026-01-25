package com.mohamad4444.github;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a database entity for code generation.
 * 
 * <p>
 * When applied to a class, the annotation processor generates a corresponding
 * database access class (suffixed with "DB") that extends {@link AbstractDB}.
 * The generated class includes a {@code buildDTO} method that maps database
 * ResultSet rows to instances of the annotated class.
 * 
 * <p>
 * <b>Requirements:</b>
 * <ul>
 * <li>The annotated class must have a constructor accepting all {@link DbField}
 * annotated fields</li>
 * <li>Fields must be annotated with {@link DbField} to be included in the
 * mapping</li>
 * <li>Constructor parameter order must match field declaration order</li>
 * </ul>
 * 
 * <p>
 * <b>Example:</b>
 * 
 * <pre>
 * {
 *     &#64;code
 *     &#64;DbEntity(tableName = "users")
 *     public class UserDTO {
 *         &#64;DbField(columnName = "id", isPrimaryKey = true)
 *         private int id;
 * 
 *         @DbField(columnName = "username")
 *         private String username;
 * 
 *         // Constructor required
 *         public UserDTO(int id, String username) {
 *             this.id = id;
 *             this.username = username;
 *         }
 *     }
 *     // Generates: UserDTODB.java
 * }
 * </pre>
 * 
 * @see DbField
 * @see AbstractDB
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface DbEntity {
    /**
     * The name of the database table this entity maps to.
     * 
     * @return the table name
     */
    String tableName();
}
