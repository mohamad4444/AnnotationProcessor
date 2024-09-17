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

@AutoService(Processor.class)
@SupportedAnnotationTypes({ "com.mohamad4444.github.DbEntity", "com.mohamad4444.github.DbField", "com.mohamad4444.github.Builder" }) // Process both annotations
@SupportedSourceVersion(SourceVersion.RELEASE_11) // Adjust to your Java version
public class BuilderProcessor extends AbstractProcessor {

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
   
    
    System.out.println("Processing annotations: " + annotations);
    // Step 1: Process @DbEntity annotations (classes)
    for (Element element : roundEnv.getElementsAnnotatedWith(DbEntity.class)) {
      if (element.getKind() == ElementKind.CLASS) {
        // Found a class annotated with @DbEntity
        TypeElement classElement = (TypeElement) element;
        DbEntity dbEntity = classElement.getAnnotation(DbEntity.class);

        // Step 2: Process @DbField annotations within the class
        generateDbClass(classElement, dbEntity.tableName());
      }
    }
    return true;
  }

  private void generateDbClass(TypeElement classElement, String tableName) {
    String className = classElement.getSimpleName() + "DB"; // e.g., CategoryDTO -> CategoryDB
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
              System.out.println("Field: " + enclosed.getSimpleName() + ", Type: " + fieldType + ", ResultSet Method: " + resultSetMethod);
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
   * Get the appropriate ResultSet method based on the field type.
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
