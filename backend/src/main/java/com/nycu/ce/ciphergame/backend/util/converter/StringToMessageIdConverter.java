package com.nycu.ce.ciphergame.backend.util.converter;

import org.springframework.stereotype.Component;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import com.nycu.ce.ciphergame.backend.entity.id.MessageId;

@Component
public class StringToMessageIdConverter implements Converter<String, MessageId> {

    @Override
    public @NonNull
    MessageId convert(@NonNull String source) {
        return MessageId.fromString(source);
    }
}
