package com.mohamad4444.github;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE) // Applied to classes
@Retention(RetentionPolicy.SOURCE) // Available at compile-time for annotation processing
public @interface DbEntity {
    String tableName(); // Custom attribute to specify the table name
}
