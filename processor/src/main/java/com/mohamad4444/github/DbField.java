package com.mohamad4444.github;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)  // Ensure the annotation is available at compile-time for processing
public @interface DbField {
    String columnName(); // Map field to a column in the database
    boolean isPrimaryKey() default false; // To mark the primary key field
}