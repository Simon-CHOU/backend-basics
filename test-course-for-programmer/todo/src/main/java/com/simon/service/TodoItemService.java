package com.simon.service;

import com.simon.dao.TodoItemRepository;
import com.simon.model.TodoIndexParameter;
import com.simon.model.TodoItem;
import com.simon.model.TodoParameter;

import java.util.Optional;

public class TodoItemService {
    private TodoItemRepository repository;

    public TodoItemService(TodoItemRepository repository) {
        this.repository = repository;
    }

    public TodoItem addTodoItem(final TodoParameter todoParameter) {
        if (todoParameter == null) {
            throw new IllegalArgumentException("Null or empty content is not allowed");
        }
        final TodoItem todoItem = new TodoItem(todoParameter.getContent());
        return this.repository.save(todoItem);
    }

//    public TodoItem markTodoItemDone(TodoIndexParameter index){
//        return null;
//    }


    public Optional<TodoItem> markTodoItemDone(TodoIndexParameter index) {
        return Optional.of(new TodoItem("a"));
    }
}
