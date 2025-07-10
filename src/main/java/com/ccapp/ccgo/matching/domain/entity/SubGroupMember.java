package com.ccapp.ccgo.matching.domain.entity;

import com.ccapp.ccgo.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sub_group_member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubGroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 어떤 SubGroup 에 소속되었는지
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_group_id", nullable = false)
    private SubGroup subGroup;

    /**
     * 소속된 사용자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
