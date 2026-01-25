
package com.mohamad4444.github;

import com.mohamad4444.github.Builder;

/**
 * Example class demonstrating the {@link Builder} annotation.
 * 
 * <p>
 * <b>⚠️ NOTE:</b> The {@code @Builder} annotation is not yet implemented.
 * This class serves as a placeholder example for future builder pattern
 * code generation functionality.
 * 
 * <p>
 * When implemented, the processor would generate an {@code EmployeeBuilder}
 * class with fluent setter methods and a {@code build()} method.
 * 
 * <p>
 * <b>Planned Generated Code:</b>
 * 
 * <pre>{@code
 * public class EmployeeBuilder {
 *     private String name;
 *     private int age;
 * 
 *     public EmployeeBuilder name(String value) {
 *         name = value;
 *         return this;
 *     }
 * 
 *     public EmployeeBuilder age(int value) {
 *         age = value;
 *         return this;
 *     }
 * 
 *     public Employee build() {
 *         return new Employee(name, age);
 *     }
 * }
 * }</pre>
 * 
 * <p>
 * <b>Planned Usage:</b>
 * 
 * <pre>{@code
 * Employee emp = new EmployeeBuilder()
 *         .name("John Doe")
 *         .age(30)
 *         .build();
 * }</pre>
 * 
 * @see Builder
 */
@SuppressWarnings("deprecation") // Builder is not yet implemented
@Builder
public class Employee {

    private String name;
    private int age;

    public Employee(String name, int age) {
        this.name = name;
        this.age = age;
    }

}
