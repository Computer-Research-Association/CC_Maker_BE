package com.ccapp.ccgo.mission.entity;

import com.ccapp.ccgo.team.entity.Team;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "team_mission")
public class TeamMission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 팀과 다대일 관계 (여러 미션이 하나 팀에 속함)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    // 미션 템플릿과 다대일 관계 (여러 팀 미션이 하나 미션 템플릿에 기반)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_template_id")
    private MissionTemplate missionTemplate;

    public TeamMission(Team team, MissionTemplate missionTemplate) {
        this.team = team;
        this.missionTemplate = missionTemplate;
    }
}
