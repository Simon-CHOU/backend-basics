package com.example.chain.standard;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SupportRequest {
    private SupportLevel level;
    private String content;
}
