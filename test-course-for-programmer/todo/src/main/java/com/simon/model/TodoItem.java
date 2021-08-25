package com.simon.model;

import lombok.Getter;

@Getter
public class TodoItem {
    private final String content;

    public TodoItem(final String content) {
        this.content = content;
    }
}
