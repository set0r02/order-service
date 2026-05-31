package com.innowise.orderservice.dto;

import com.innowise.orderservice.model.OrderItem;
import com.innowise.orderservice.model.Status;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public record OrderOutputDto(
        Long id,

        Long userId,

        Status status,

        double totalPrice,

        boolean deleted,

        List<OrderItem> orderItems,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) implements Serializable {
}
