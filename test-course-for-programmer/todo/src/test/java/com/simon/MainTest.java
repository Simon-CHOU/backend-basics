package com.simon;

import com.simon.dao.TodoItemRepository;
import com.simon.model.TodoItem;
import com.simon.model.TodoParameter;
import com.simon.service.TodoItemService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MainTest {

    @Test
    public void should_add_todo_item() {
        TodoItemRepository repository = mock(TodoItemRepository.class);
        when(repository.save(any())).then(returnsFirstArg());
        TodoItemService service = new TodoItemService(repository);
        TodoItem item = service.addTodoItem(new TodoParameter("foo"));
        assertThat(item.getContent()).isEqualTo("foo");
    }


    @Test
    public void should_throw_exception_for_null_todo_item() {
        TodoItemRepository repository = mock(TodoItemRepository.class);
        when(repository.save(any())).then(returnsFirstArg());
        TodoItemService service = new TodoItemService(repository);
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> service.addTodoItem(null));
    }
}