package com.simon.mastering_spring_data_jpa.convertor;

import com.simon.mastering_spring_data_jpa.dto.Gender;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class GenderConverter implements AttributeConverter<Gender, String> {
    @Override
    public String convertToDatabaseColumn(Gender attribute) {
        return attribute.getDbValue();
    }

    @Override
    public Gender convertToEntityAttribute(String dbData) {
        return Gender.getGenderByValue(dbData).orElse(Gender.BOY);
    }
}
