package com.simon.mastering_spring_data_jpa.dao;

import com.simon.mastering_spring_data_jpa.convertor.GenderConverter;
import com.simon.mastering_spring_data_jpa.enums.Gender;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "students")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Student {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "name")
    private String name;
    @Column(name = "email")
    private String email;
    @Column(name = "age")
    private Integer age;
    @Column(name = "gender", columnDefinition = "CHAR(1)")
    @Convert(converter = GenderConverter.class)
    private Gender gender;
}
