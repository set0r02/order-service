package com.innowise.orderservice.dto.input;

import com.innowise.orderservice.model.enums.Status;
import jakarta.validation.constraints.NotNull;

public record OrderUpdateInputDto(

        @NotNull(message = "Status is required")
        Status status
) {
}
