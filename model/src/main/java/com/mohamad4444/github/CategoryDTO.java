package com.mohamad4444.github;

import com.mohamad4444.github.DbEntity;
import com.mohamad4444.github.DbField;

import lombok.*;

/**
 * Example DTO demonstrating the use of {@link DbEntity} and {@link DbField}
 * annotations.
 * 
 * <p>
 * This class represents a category entity from the "ev_category" database
 * table.
 * When compiled, the annotation processor generates a {@code CategoryDTODB}
 * class
 * that extends {@code AbstractDB<CategoryDTO>} with automatic ResultSet
 * mapping.
 * 
 * <p>
 * <b>Generated Code:</b><br>
 * The processor generates {@code CategoryDTODB.java} in
 * {@code target/generated-sources/annotations/} with a {@code buildDTO} method
 * that:
 * 
 * <pre>{@code
 * protected CategoryDTO buildDTO(ResultSet rs) throws SQLException {
 *     return new CategoryDTO(
 *             rs.getInt("ekid"),
 *             rs.getString("category_name"),
 *             rs.getString("color"),
 *             rs.getInt("evid"));
 * }
 * }</pre>
 * 
 * <p>
 * <b>Usage:</b><br>
 * The generated class can be used in your data access layer:
 * 
 * <pre>{@code
 * CategoryDTODB categoryDb = new CategoryDTODB();
 * List<CategoryDTO> categories = categoryDb.executeQuery(
 *         connection,
 *         "SELECT * FROM ev_category");
 * }</pre>
 * 
 * @see DbEntity
 * @see DbField
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@DbEntity(tableName = "ev_category")
public class CategoryDTO {

    @DbField(columnName = "ekid", isPrimaryKey = true)
    private int ekid;

    @DbField(columnName = "category_name")
    private String categoryName;

    @DbField(columnName = "color")
    private String color;

    @DbField(columnName = "evid")
    private int evid;
}
