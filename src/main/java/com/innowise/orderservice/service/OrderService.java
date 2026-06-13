package com.innowise.orderservice.service;

import com.innowise.orderservice.dto.input.OrderInputDto;
import com.innowise.orderservice.dto.input.OrderUpdateInputDto;
import com.innowise.orderservice.dto.output.OrderWithUserDto;
import com.innowise.orderservice.model.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {

    OrderWithUserDto createOrder(OrderInputDto orderInputDto);

    OrderWithUserDto getOrderById(Long id);

    Page<OrderWithUserDto> getOrders(Pageable pageable, LocalDateTime from, LocalDateTime to, List<Status> statuses);

    List<OrderWithUserDto> getOrdersByUserId(Long userId);

    OrderWithUserDto updateOrderById(Long id,  OrderUpdateInputDto orderUpdateInputDto);

    OrderWithUserDto deleteOrderById(Long id);

}
