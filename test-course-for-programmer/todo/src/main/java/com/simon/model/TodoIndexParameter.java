package com.simon.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TodoIndexParameter {
    private Integer index;

    public static TodoIndexParameter of(Integer index) {
        return new TodoIndexParameter(index);
    }
}
