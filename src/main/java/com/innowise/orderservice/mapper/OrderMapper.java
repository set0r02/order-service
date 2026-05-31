package com.innowise.orderservice.mapper;

import com.innowise.orderservice.dto.OrderInputDto;
import com.innowise.orderservice.dto.OrderOutputDto;
import com.innowise.orderservice.model.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = OrderItemMapper.class)
public interface OrderMapper {

    OrderOutputDto toDto(Order order);

}
