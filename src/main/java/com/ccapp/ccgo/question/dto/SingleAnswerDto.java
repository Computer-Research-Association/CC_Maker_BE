package com.ccapp.ccgo.question.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SingleAnswerDto {
    private Long questionId;
    private Integer score;
}
