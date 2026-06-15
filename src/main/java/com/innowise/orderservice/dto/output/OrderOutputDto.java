package com.innowise.orderservice.dto.output;

import com.innowise.orderservice.model.Status;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderOutputDto(
        Long id,

        Long userId,

        Status status,

        BigDecimal totalPrice,

        List<OrderItemOutputDto> items,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) implements Serializable {
}
