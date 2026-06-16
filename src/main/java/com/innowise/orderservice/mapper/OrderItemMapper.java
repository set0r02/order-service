package com.innowise.orderservice.mapper;

import com.innowise.orderservice.dto.output.OrderItemOutputDto;
import com.innowise.orderservice.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "itemName", source = "item.name")
    @Mapping(target = "price", source = "item.price")
    @Mapping(target = "quantity", source = "quantity")
    OrderItemOutputDto toDto(OrderItem orderItem);

}
