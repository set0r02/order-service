package com.innowise.orderservice.dto.output;

public record OrderItemOutputDto(

        Long id,

        Long itemId,

        String itemName,

        double price,

        int quantity
) {



}
