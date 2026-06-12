package com.innowise.orderservice.service;

import com.innowise.orderservice.dto.input.OrderInputDto;
import com.innowise.orderservice.dto.output.OrderOutputDto;
import com.innowise.orderservice.model.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {

    OrderOutputDto createOrder(OrderInputDto orderInputDto);

    OrderOutputDto getOrderById(Long id);

    Page<OrderOutputDto> getOrders(Pageable pageable, LocalDateTime from, LocalDateTime to, List<Status> statuses);

    List<OrderOutputDto> getOrdersByUserId(Long userId);

    OrderOutputDto updateOrderById(Long id, OrderInputDto orderInputDto);

    OrderOutputDto deleteOrderById(Long id);

}
