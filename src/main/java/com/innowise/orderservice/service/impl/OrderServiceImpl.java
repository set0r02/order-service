package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.dto.OrderInputDto;
import com.innowise.orderservice.dto.OrderOutputDto;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.model.Order;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;


    public OrderOutputDto createOrder(OrderInputDto orderInputDto){
        Order order = orderMapper.toEntity(orderInputDto);
        order.setDeleted(false);
        return orderMapper.toDto(orderRepository.save(order));
    }

}
