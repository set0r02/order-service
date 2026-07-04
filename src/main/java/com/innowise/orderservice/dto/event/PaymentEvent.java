package com.innowise.orderservice.dto.event;

import com.innowise.orderservice.model.enums.PaymentStatus;
import com.innowise.orderservice.model.enums.Status;

public record PaymentEvent(
        Long orderId,
        PaymentStatus paymentStatus
) {
}
