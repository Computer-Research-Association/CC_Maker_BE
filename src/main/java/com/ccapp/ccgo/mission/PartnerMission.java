package com.ccapp.ccgo.mission;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PartnerMission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "template_id")
    private MissionTemplate template;

    @ManyToOne
    @JoinColumn(name = "partner_id")
    private Partner partner;

    private String title;

    private String description;

    private LocalDate dueDate;

    private boolean isActive;
}