package com.ccapp.ccgo.mission.entity;

import com.ccapp.ccgo.matching.domain.entity.SubGroup;
import com.ccapp.ccgo.team.entity.Team;
import com.ccapp.ccgo.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mission_history")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissionHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_group_id")
    private SubGroup subGroup;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", referencedColumnName = "teamId")
    private Team team;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_template_id")
    private MissionTemplate missionTemplate;
    
    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (completedAt == null) {
            completedAt = LocalDateTime.now();
        }
    }
}
