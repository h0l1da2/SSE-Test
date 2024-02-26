package com.example.sse.domain;

import lombok.Builder;

@Builder
public class User {
    private String username;
    private String name;
    private Integer age;
}
