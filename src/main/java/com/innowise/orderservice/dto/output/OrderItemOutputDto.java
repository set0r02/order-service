package com.innowise.orderservice.dto.output;

import java.math.BigDecimal;

public record OrderItemOutputDto(

        Long id,

        Long itemId,

        String itemName,

        BigDecimal price,

        Integer quantity
) {



}
