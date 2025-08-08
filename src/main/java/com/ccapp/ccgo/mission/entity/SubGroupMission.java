package com.ccapp.ccgo.mission.entity;

import com.ccapp.ccgo.matching.domain.entity.SubGroup;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "subgroup_mission")
public class SubGroupMission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subgroup_id")
    private SubGroup subGroup;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mission_template_id")
    private MissionTemplate missionTemplate;

    private boolean completed;


}
