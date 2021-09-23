package com.simon.service;

import com.google.common.collect.ImmutableList;
import com.simon.dao.TodoItemRepository;
import com.simon.model.TodoIndexParameter;
import com.simon.model.TodoItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TodoItemServiceTest {
    TodoItemService service;
    TodoItemRepository repository;

    @BeforeEach
    void setUp() {
        this.repository = mock(TodoItemRepository.class);
        this.service = new TodoItemService(this.repository);
    }

    @Test
    public     void should_mark_todo_item_as_done() {
        when(repository.findAll()).thenReturn(ImmutableList.of(new TodoItem("foo")));
        when(repository.save(any())).then(returnsFirstArg());
        final Optional<TodoItem> todoItem = service.markTodoItemDone(TodoIndexParameter.of(1));
        assertThat(todoItem).isPresent();
        final TodoItem actual = todoItem.get();
        assertThat(actual.isDone()).isTrue();
    }
}