package com.nycu.ce.ciphergame.backend.repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nycu.ce.ciphergame.backend.entity.Group;
import com.nycu.ce.ciphergame.backend.entity.Member;
import com.nycu.ce.ciphergame.backend.entity.User;
import com.nycu.ce.ciphergame.backend.entity.id.MemberId;

@Repository
public interface MemberRepository extends JpaRepository<Member, MemberId> {

    @Query("""
        SELECT gm.group
        FROM Member gm
        WHERE gm.user.id = :userId
    """)
    List<Group> findAllGroups(@Param("userId") UUID userId);

    @Query("""
    SELECT gm.user
    FROM Member gm
    WHERE gm.group.id = :groupId
""")
    List<User> findAllUsers(@Param("groupId") UUID groupId);

    long countByIdIn(List<MemberId> ids);

    Set<Member> findAllByIdIn(Set<MemberId> ids);

}
