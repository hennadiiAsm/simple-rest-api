package com.example.simple.util;

public class SimpleDTO {
    private final Object data;

    private SimpleDTO(Object data) {
        this.data = data;
    }

    public static SimpleDTO of(Object data) {
        return new SimpleDTO(data);
    }

    public Object getData() {
        return data;
    }
}
