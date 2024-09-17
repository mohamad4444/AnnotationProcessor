
package com.mohamad4444.github;

import com.mohamad4444.github.Builder;

@Builder
public class Employee {

    private String name;
    private int age;

    public Employee(String name, int age) {
        this.name = name;
        this.age = age;
    }

}
