package com.innowise.orderservice.mapper;

import com.innowise.orderservice.dto.output.OrderOutputDto;
import com.innowise.orderservice.dto.output.UserDto;
import com.innowise.orderservice.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = OrderItemMapper.class)
public interface OrderMapper {

    @Mapping(source = "orderItems", target = "items")
    OrderOutputDto toDto(Order order);

}
