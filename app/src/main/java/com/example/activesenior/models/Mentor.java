// models/Mentor.java
package com.example.activesenior.models;

public class Mentor {
    private String name;
    private String field;
    private String email;

    public Mentor() {} // Firestore에서 객체 변환을 위해 기본 생성자 필요

    public Mentor(String name, String field, String email) {
        this.name = name;
        this.field = field;
        this.email = email;
    }

    public String getName() { return name; }
    public String getField() { return field; }
    public String getEmail() { return email; }
}
