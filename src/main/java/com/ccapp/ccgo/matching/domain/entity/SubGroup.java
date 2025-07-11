package com.ccapp.ccgo.matching.domain.entity;

import com.ccapp.ccgo.team.entity.Team;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sub_group")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 어떤 팀 안에서 만들어진 그룹인지
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    /**
     * 그룹 이름
     * 예) "A팀 매칭 그룹 1번"
     */
    @Column(nullable = false, length = 255)
    private String name;

    /**
     * 그룹당 멤버 수 (고정)
     */
    @Column(name = "member_count", nullable = false)
    private int memberCount;

}
