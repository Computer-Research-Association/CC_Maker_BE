package com.ccapp.ccgo.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerRequestDto {
    private Long userId;
    private String mbti;
    private Long teamId;
    private List<SingleAnswerDto> answers;
}
