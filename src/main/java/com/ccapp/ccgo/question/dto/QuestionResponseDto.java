package com.ccapp.ccgo.question.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionResponseDto {
    private Long id;
    private String text;
}
