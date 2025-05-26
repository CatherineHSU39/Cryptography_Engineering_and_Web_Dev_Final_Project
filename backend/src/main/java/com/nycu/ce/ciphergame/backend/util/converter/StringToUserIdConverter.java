package com.nycu.ce.ciphergame.backend.util.converter;

import org.springframework.stereotype.Component;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import com.nycu.ce.ciphergame.backend.entity.id.UserId;

@Component
public class StringToUserIdConverter implements Converter<String, UserId> {

    @Override
    public @NonNull
    UserId convert(@NonNull String source) {
        return UserId.fromString(source);
    }
}
