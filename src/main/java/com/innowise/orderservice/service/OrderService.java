package com.innowise.orderservice.service;

import com.innowise.orderservice.dto.input.OrderInputDto;
import com.innowise.orderservice.dto.output.OrderOutputDto;

public interface OrderService {

    OrderOutputDto createOrder(OrderInputDto orderInputDto);

    OrderOutputDto getOrderById(Long id);

}
