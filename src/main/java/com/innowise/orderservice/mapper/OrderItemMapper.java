package com.innowise.orderservice.mapper;

import com.innowise.orderservice.dto.output.OrderItemOutputDto;
import com.innowise.orderservice.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "itemId", source = "item.id")
    OrderItemOutputDto toDto(OrderItem orderItem);

}
