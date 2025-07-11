package com.ccapp.ccgo.question.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerRequestDto {
    private String mbti;
    private Long teamId;
    private List<SingleAnswerDto> answers;
}
