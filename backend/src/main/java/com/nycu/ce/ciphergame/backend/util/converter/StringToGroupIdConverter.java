package com.nycu.ce.ciphergame.backend.util.converter;

import org.springframework.stereotype.Component;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import com.nycu.ce.ciphergame.backend.entity.id.GroupId;

@Component
public class StringToGroupIdConverter implements Converter<String, GroupId> {

    @Override
    public @NonNull
    GroupId convert(@NonNull String source) {
        return GroupId.fromString(source);
    }
}
