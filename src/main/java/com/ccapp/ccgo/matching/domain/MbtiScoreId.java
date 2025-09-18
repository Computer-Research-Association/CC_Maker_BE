package com.ccapp.ccgo.matching.domain;

import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MbtiScoreId implements Serializable {
    private String fromMbti;
    private String toMbti;
}
