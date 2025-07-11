package com.ccapp.ccgo.question.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionRequestDto {
    private Long teamId;
    private List<String> questions;
}
