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

    // SubGroup 기준으로 멤버들 조회
    List<SubGroupMember> findBySubGroup(SubGroup subGroup);

    long countBySubGroup_Id(Long subGroupId);

    //subgroup 자신을 제외한 팀원 이름조회
    @Query("SELECT m FROM SubGroupMember m WHERE m.subGroup = " +
            "(SELECT sm.subGroup FROM SubGroupMember sm WHERE sm.user.id = :userId)")
    List<SubGroupMember> findBySameSubGroup(@Param("userId") Long userId);


    @Query("SELECT s.user FROM SubGroupMember s WHERE s.subGroup.id = :subGroupId")
    List<User> findUsersBySubGroupId(@Param("subGroupId") Long subGroupId);



    @Query("SELECT sgm.subGroup.id FROM SubGroupMember sgm WHERE sgm.subGroup.team.teamId = :teamId AND sgm.user.id = :userId")
    Optional<Long> findSubGroupIdByTeamIdAndUserId(@Param("teamId") Long teamId, @Param("userId") Long userId);


}
