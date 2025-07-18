package com.ccapp.ccgo.matching.repository;

import com.ccapp.ccgo.matching.domain.entity.SubGroup;
import com.ccapp.ccgo.matching.domain.entity.SubGroupMember;
import com.ccapp.ccgo.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubGroupMemberRepository extends JpaRepository<SubGroupMember, Long> {
    List<SubGroupMember> findBySubGroup_Id(Long subGroupId);

    @Query("""
    SELECT m.user 
    FROM SubGroupMember m
    WHERE m.subGroup = (
        SELECT sm.subGroup 
        FROM SubGroupMember sm 
        WHERE sm.user.id = :userId AND sm.subGroup.team.teamId = :teamId
    )
    AND m.user.id <> :userId
    """)
    List<User> findTeamMatchedMembersExcludingUser(@Param("userId") Long userId, @Param("teamId") Long teamId);


    long countBySubGroup_Id(Long subGroupId);

    @Query("SELECT sgm.subGroup.id FROM SubGroupMember sgm WHERE sgm.subGroup.team.teamId = :teamId AND sgm.user.id = :userId")
    Optional<Long> findSubGroupIdByTeamIdAndUserId(@Param("teamId") Long teamId, @Param("userId") Long userId);



}
