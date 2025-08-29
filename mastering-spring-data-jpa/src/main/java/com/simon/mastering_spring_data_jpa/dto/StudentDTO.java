package com.simon.mastering_spring_data_jpa.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentDTO {
    private String name;
    private String email;
    private Integer minAge;
    private Integer maxAge;
}