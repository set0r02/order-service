package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.client.UserServiceClient;
import com.innowise.orderservice.dto.input.OrderInputDto;
import com.innowise.orderservice.dto.input.OrderItemInputDto;
import com.innowise.orderservice.dto.input.OrderUpdateInputDto;
import com.innowise.orderservice.dto.output.OrderOutputDto;
import com.innowise.orderservice.dto.output.OrderWithUserDto;
import com.innowise.orderservice.dto.output.UserDto;
import com.innowise.orderservice.exception.ItemNotFoundException;
import com.innowise.orderservice.exception.OrderNotFoundException;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.model.Item;
import com.innowise.orderservice.model.Order;
import com.innowise.orderservice.model.OrderItem;
import com.innowise.orderservice.model.Status;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.service.OrderService;
import com.innowise.orderservice.specifications.OrderSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final UserServiceClient userServiceClient;

    @Override
    public OrderWithUserDto createOrder(OrderInputDto orderInputDto){

        Order order = Order.builder()
                .userId(orderInputDto.userId())
                .status(Status.CREATED)
                .deleted(false)
                .build();

        List<OrderItem> orderItemsList = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        for(OrderItemInputDto orderItemInputDto : orderInputDto.items()){
            Item item = itemRepository.findById(orderItemInputDto.itemId()).orElseThrow(
                    () -> new ItemNotFoundException("Item not found")
            );

            OrderItem orderItem = OrderItem.builder()
                    .quantity(orderItemInputDto.quantity())
                    .build();

            orderItem.setOrder(order);
            orderItem.setItem(item);

            orderItemsList.add(orderItem);

            totalPrice = totalPrice.add(item.getPrice().multiply(BigDecimal.valueOf(orderItemInputDto.quantity())));
        }

        order.setOrderItems(orderItemsList);
        order.setTotalPrice(totalPrice);

        Order savedOrder = orderRepository.save(order);

        OrderOutputDto orderOutputDto = orderMapper.toDto(savedOrder);
        UserDto userDto = userServiceClient.getUserById(orderInputDto.userId());

        return new OrderWithUserDto(orderOutputDto,userDto);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderWithUserDto getOrderById(Long id){
        Order order = orderRepository.findById(id)
                .filter(ord -> !ord.isDeleted())
                .orElseThrow(() ->
                        new OrderNotFoundException("Order not found"));

        OrderOutputDto orderOutputDto = orderMapper.toDto(order);
        UserDto userDto = userServiceClient.getUserById(orderOutputDto.userId());

        return new OrderWithUserDto(orderOutputDto,userDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderWithUserDto> getOrders(Pageable pageable, LocalDateTime from, LocalDateTime to, List<Status> statuses) {
        Specification<Order> specification = Specification
                .where(OrderSpecifications.createdBetween(from,to))
                .and(OrderSpecifications.hasStatuses(statuses));

        return orderRepository.findAll(specification,pageable)
                .map(order -> {
                    OrderOutputDto orderOutputDto = orderMapper.toDto(order);
                    UserDto userDto = userServiceClient.getUserById(order.getUserId());
                    return new OrderWithUserDto(orderOutputDto,userDto);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderWithUserDto> getOrdersByUserId(Long userId) {

        return orderRepository.findByUserId(userId)
                .stream()
                .map(order -> {
                    OrderOutputDto orderOutputDto = orderMapper.toDto(order);
                    UserDto userDto = userServiceClient.getUserById(userId);
                    return new OrderWithUserDto(orderOutputDto,userDto);
                }).toList();
    }

    @Override
    public OrderWithUserDto updateOrderById(Long id, OrderUpdateInputDto orderUpdateInputDto) {
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new OrderNotFoundException("Order not found")
        );
        order.setStatus(orderUpdateInputDto.status());

        Order savedOrder = orderRepository.save(order);

        OrderOutputDto orderOutputDto = orderMapper.toDto(order);
        UserDto userDto = userServiceClient.getUserById(order.getUserId());

        return new OrderWithUserDto(orderOutputDto,userDto);
    }

    @Override
    public OrderWithUserDto deleteOrderById(Long id) {

        Order order = softDelete(id);

        OrderOutputDto orderOutputDto = orderMapper.toDto(order);
        UserDto userDto = userServiceClient.getUserById(order.getUserId());

        return new OrderWithUserDto(orderOutputDto,userDto);
    }

    private Order softDelete(Long id){
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new OrderNotFoundException("Order not found")
        );
        order.setDeleted(true);
        return orderRepository.save(order);
    }

}
