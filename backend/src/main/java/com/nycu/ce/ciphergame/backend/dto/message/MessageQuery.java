package com.nycu.ce.ciphergame.backend.dto.message;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MessageQuery {

    @Min(value = 1, message = "Limit must be at least 1")
    @Max(value = 100, message = "Limit must not exceed 100")
    private int limit = 20;

    @Min(value = 0, message = "Offset must be non-negative")
    private int offset = 0;

    public Pageable toPageable(Sort sort) {
        int page = offset / limit;
        return PageRequest.of(page, limit, sort);
    }

    public Pageable toPageable() {
        return toPageable(Sort.unsorted());
    }
}
