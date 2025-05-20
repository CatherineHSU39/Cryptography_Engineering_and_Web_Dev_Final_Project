package com.nycu.ce.ciphergame.backend.repository;

import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nycu.ce.ciphergame.backend.entity.GroupMember;
import com.nycu.ce.ciphergame.backend.entity.GroupMemberId;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, GroupMemberId> {
    @Query("SELECT gm.id.userId FROM GroupMember gm WHERE gm.id.groupId = :groupId")
    Set<UUID> findUserIdsByGroupId(@Param("groupId") UUID groupId);
}
