package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.dto.OrderInputDto;
import com.innowise.orderservice.dto.OrderItemInputDto;
import com.innowise.orderservice.dto.OrderOutputDto;
import com.innowise.orderservice.exception.NotFoundException;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.model.Item;
import com.innowise.orderservice.model.Order;
import com.innowise.orderservice.model.OrderItem;
import com.innowise.orderservice.model.Status;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;


    public OrderOutputDto createOrder(OrderInputDto orderInputDto){
        Order order = Order.builder()
                .userId(orderInputDto.userId())
                .status(Status.CREATED)
                .deleted(false)
                .build();

        List<OrderItem> orderItemsList = new ArrayList<>();
        double totalPrice = 0;

        for(OrderItemInputDto orderItemInputDto : orderInputDto.items()){
            Item item = itemRepository.findById(orderItemInputDto.itemId()).orElseThrow(
                    () -> new NotFoundException("Item not found")
            );

            OrderItem orderItem = OrderItem.builder()
                    .quantity(orderItemInputDto.quantity())
                    .build();

            orderItem.setOrder(order);
            orderItem.setItem(item);

            orderItemsList.add(orderItem);

            totalPrice = item.getPrice() * orderItemInputDto.quantity();
        }

        order.setOrderItems(orderItemsList);
        order.setTotalPrice(totalPrice);

        return orderMapper.toDto(orderRepository.save(order));
    }

    public OrderOutputDto getOrderById(Long id){
        Order order = orderRepository.findById(id)
                .filter(ord -> !ord.isDeleted())
                .orElseThrow(() ->
                        new NotFoundException("Order not found"));

        return orderMapper.toDto(order);
    }

}
