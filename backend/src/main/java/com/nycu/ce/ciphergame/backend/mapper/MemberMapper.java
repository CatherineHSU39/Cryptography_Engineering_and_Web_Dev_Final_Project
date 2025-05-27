package com.nycu.ce.ciphergame.backend.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.nycu.ce.ciphergame.backend.dto.member.MemberResponse;
import com.nycu.ce.ciphergame.backend.entity.Member;

@Component
public class MemberMapper {

    public MemberResponse toDTO(Member member) {
        return MemberResponse.builder()
                .userId(member.getUser().getId())
                .username(member.getUser().getUsername())
                .joinAt(member.getJoinedAt())
                .build();
    }

    public Set<MemberResponse> toDTO(Set<Member> members) {
        return members.stream()
                .map(this::toDTO)
                .collect(Collectors.toSet());
    }
}
