package com.nycu.ce.ciphergame.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nycu.ce.ciphergame.backend.dto.message.MessageRequest;
import com.nycu.ce.ciphergame.backend.dto.message.MessageResponse;
import com.nycu.ce.ciphergame.backend.entity.Message;

@Mapper(componentModel = "spring")
public interface MessageMapper {
    Message toEntity(MessageRequest dto);

    @Mapping(source = "sender.id", target = "senderId")
    @Mapping(source = "group.id", target = "groupId")
    @Mapping(source = "id", target = "messageId")
    MessageResponse toDTO(Message message);
}
