package com.mohamad4444.github;

import javax.annotation.processing.*;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.SourceVersion;
import java.util.Set;
import java.io.Writer;
import javax.tools.JavaFileObject;

import com.google.auto.service.AutoService;

/**
 * Annotation processor for generating database entity classes.
 * 
 * <p>
 * This processor handles {@link DbEntity} and {@link DbField} annotations,
 * generating database access classes that extend {@link AbstractDB}. For each
 * class annotated with {@code @DbEntity}, a corresponding class is generated
 * with a "DB" suffix that includes proper ResultSet mapping logic.
 * 
 * <p>
 * The processor uses Google's AutoService to automatically register itself
 * as an annotation processor, making it available during compile-time without
 * manual configuration.
 * 
 * <p>
 * <b>Supported Annotations:</b>
 * <ul>
 * <li>{@link DbEntity} - Class-level annotation for database entities</li>
 * <li>{@link DbField} - Field-level annotation for column mapping</li>
 * <li>{@link Builder} - (Not yet implemented) For builder pattern
 * generation</li>
 * </ul>
 * 
 * <p>
 * <b>Processing Flow:</b>
 * <ol>
 * <li>Scan for classes annotated with {@code @DbEntity}</li>
 * <li>For each entity class, extract its fields with {@code @DbField}</li>
 * <li>Generate a new class extending {@code AbstractDB<OriginalClass>}</li>
 * <li>Implement the {@code buildDTO} method with proper type mapping</li>
 * <li>Write the generated class to
 * {@code target/generated-sources/annotations/}</li>
 * </ol>
 * 
 * <p>
 * <b>Example:</b>
 * 
 * <pre>
 * {
 *   &#64;code
 *   // Input class
 *   &#64;DbEntity(tableName = "users")
 *   public class UserDTO {
 *     &#64;DbField(columnName = "id", isPrimaryKey = true)
 *     private int id;
 *     &#64;DbField(columnName = "username")
 *     private String username;
 *   }
 * 
 *   // Generated class: UserDTODB.java
 *   public class UserDTODB extends AbstractDB<UserDTO> {
 *     @Override
 *     protected UserDTO buildDTO(ResultSet rs) throws SQLException {
 *       return new UserDTO(rs.getInt("id"), rs.getString("username"));
 *     }
 *   }
 * }
 * </pre>
 * 
 * @see DbEntity
 * @see DbField
 * @see AbstractDB
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes({
    "com.mohamad4444.github.DbEntity",
    "com.mohamad4444.github.DbField",
    "com.mohamad4444.github.Builder"
})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class BuilderProcessor extends AbstractProcessor {

  /**
   * Processes the {@link DbEntity} and {@link DbField} annotations.
   * 
   * <p>
   * This method is called by the Java compiler during annotation processing.
   * It scans for classes annotated with {@code @DbEntity} and generates
   * corresponding database access classes.
   * 
   * <p>
   * The processing happens in rounds. This method returns {@code true} to
   * claim the annotations, preventing other processors from processing them.
   * 
   * @param annotations the set of annotation types requested to be processed
   * @param roundEnv    environment for information about the current and prior
   *                    rounds
   * @return {@code true} to claim these annotations, {@code false} otherwise
   */
  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    // Process @DbEntity annotations (class-level)
    for (Element element : roundEnv.getElementsAnnotatedWith(DbEntity.class)) {
      if (element.getKind() == ElementKind.CLASS) {
        TypeElement classElement = (TypeElement) element;
        DbEntity dbEntity = classElement.getAnnotation(DbEntity.class);

        // Generate the database access class
        generateDbClass(classElement, dbEntity.tableName());
      }
    }
    return true;
  }

  /**
   * Generates a database access class for the given entity.
   * 
   * <p>
   * This method creates a new Java source file with the name
   * {@code <OriginalClassName>DB.java} that extends {@link AbstractDB}.
   * The generated class includes:
   * <ul>
   * <li>Proper package declaration and imports</li>
   * <li>A {@code buildDTO} method that maps ResultSet to the entity</li>
   * <li>Type-appropriate ResultSet getter methods for each field</li>
   * </ul>
   * 
   * <p>
   * Generated files are written to the standard annotation processor
   * output directory: {@code target/generated-sources/annotations/}
   * 
   * @param classElement the type element of the class annotated with
   *                     {@link DbEntity}
   * @param tableName    the database table name (currently unused in generation)
   */
  private void generateDbClass(TypeElement classElement, String tableName) {
    String className = classElement.getSimpleName() + "DB";
    String packageName = processingEnv.getElementUtils().getPackageOf(classElement).toString();

    try {
      // Create the new source file for the DB class
      JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(packageName + "." + className);
      try (Writer writer = builderFile.openWriter()) {
        StringBuilder sb = new StringBuilder();
        // Write package declaration
        sb.append("package " + packageName + ";\n\n");

        // Import necessary classes
        sb.append("import java.sql.Connection;\n");
        sb.append("import java.sql.ResultSet;\n");
        sb.append("import java.sql.SQLException;\n");
        sb.append("import java.util.List;\n");
        sb.append("import " + packageName + "." + classElement.getSimpleName() + ";\n");

        // Start DB class
        sb.append("public class " + className + " extends AbstractDB<" + classElement.getSimpleName() + "> {\n\n");

        // Step 3: Generate the buildDTO method
        sb.append("    @Override\n");
        sb.append("    protected " + classElement.getSimpleName() + " buildDTO(ResultSet rs) throws SQLException {\n");
        sb.append("        return new " + classElement.getSimpleName() + "(");

        // Iterate over fields and process @DbField annotations
        boolean first = true;
        for (Element enclosed : classElement.getEnclosedElements()) {
          if (enclosed.getKind() == ElementKind.FIELD) {
            DbField dbField = enclosed.getAnnotation(DbField.class);
            if (dbField != null) {
              // Get the type of the field
              TypeMirror fieldType = enclosed.asType();

              // Based on the field type, select the correct ResultSet method
              String resultSetMethod = getResultSetMethod(fieldType);

              // Add code to read the field from the ResultSet
              if (!first) {
                sb.append(", ");
              }
              sb.append("rs." + resultSetMethod + "(\"" + dbField.columnName() + "\")");
              first = false;
            }
          }
        }

        sb.append(");\n    }\n\n");

        // Additional methods like findAll, findById, etc., can be generated here
        sb.append("}\n");

        writer.write(sb.toString());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Determines the appropriate ResultSet getter method for a field type.
   * 
   * <p>
   * This method maps Java types to corresponding JDBC ResultSet getter methods.
   * It handles both primitive types and common Java/SQL declared types.
   * 
   * <p>
   * <b>Type Mappings:</b>
   * 
   * <pre>
   * int         → getInt
   * long        → getLong
   * float       → getFloat
   * double      → getDouble
   * boolean     → getBoolean
   * String      → getString
   * Date        → getDate
   * Timestamp   → getTimestamp
   * Other       → getObject (fallback)
   * </pre>
   * 
   * @param fieldType the TypeMirror representing the field's type
   * @return the name of the ResultSet getter method (without parentheses)
   */
  private String getResultSetMethod(TypeMirror fieldType) {
    if (fieldType.getKind() == TypeKind.INT) {
      return "getInt";
    } else if (fieldType.getKind() == TypeKind.BOOLEAN) {
      return "getBoolean";
    } else if (fieldType.getKind() == TypeKind.LONG) {
      return "getLong";
    } else if (fieldType.getKind() == TypeKind.FLOAT) {
      return "getFloat";
    } else if (fieldType.getKind() == TypeKind.DOUBLE) {
      return "getDouble";
    } else if (fieldType.getKind() == TypeKind.DECLARED) {
      // Handle commonly used declared types, e.g., String, Date
      if (fieldType.toString().equals("java.lang.String")) {
        return "getString";
      } else if (fieldType.toString().equals("java.sql.Date")) {
        return "getDate";
      } else if (fieldType.toString().equals("java.sql.Timestamp")) {
        return "getTimestamp";
      }
    }
    // Default to getObject if the type isn't explicitly handled
    return "getObject";
  }
}
