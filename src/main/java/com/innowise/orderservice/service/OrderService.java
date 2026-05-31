package com.innowise.orderservice.service;

import com.innowise.orderservice.dto.OrderInputDto;
import com.innowise.orderservice.dto.OrderOutputDto;

public interface OrderService {

    OrderOutputDto createOrder(OrderInputDto orderInputDto);

    OrderOutputDto getOrderById(Long id);

}
