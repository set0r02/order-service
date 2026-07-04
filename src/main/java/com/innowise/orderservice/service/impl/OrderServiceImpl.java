package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.client.UserServiceClient;
import com.innowise.orderservice.client.service.UserServiceGateway;
import com.innowise.orderservice.dto.input.OrderInputDto;
import com.innowise.orderservice.dto.input.OrderItemInputDto;
import com.innowise.orderservice.dto.input.OrderUpdateInputDto;
import com.innowise.orderservice.dto.output.OrderWithUserDto;
import com.innowise.orderservice.dto.output.UserDto;
import com.innowise.orderservice.exception.ItemNotFoundException;
import com.innowise.orderservice.exception.OrderNotFoundException;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.model.Item;
import com.innowise.orderservice.model.Order;
import com.innowise.orderservice.model.OrderItem;
import com.innowise.orderservice.model.enums.PaymentStatus;
import com.innowise.orderservice.model.enums.Status;
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
    private final UserServiceGateway userServiceGateway;

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

        return buildOrderWithUserDto(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderWithUserDto getOrderById(Long id){
        Order order = orderRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new OrderNotFoundException("Order not found"));

        return buildOrderWithUserDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderWithUserDto> getOrders(Pageable pageable, LocalDateTime from, LocalDateTime to, List<Status> statuses) {
        Specification<Order> specification = Specification
                .where(OrderSpecifications.notDeleted())
                .and(OrderSpecifications.createdBetween(from,to))
                .and(OrderSpecifications.hasStatuses(statuses));

        return orderRepository.findAll(specification,pageable)
                .map(this::buildOrderWithUserDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderWithUserDto> getOrdersByUserId(Long userId) {

        UserDto userDto = userServiceGateway.getUser(userId);

        return orderRepository.findByUserIdAndDeletedFalse(userId)
                .stream()
                .map(order -> new OrderWithUserDto(
                        orderMapper.toDto(order),
                        userDto
                ))
                .toList();
    }

    @Override
    public OrderWithUserDto updateOrderById(Long id, OrderUpdateInputDto orderUpdateInputDto) {
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new OrderNotFoundException("Order not found")
        );
        order.setStatus(orderUpdateInputDto.status());

        Order savedOrder = orderRepository.save(order);
        return buildOrderWithUserDto(savedOrder);
    }

    @Override
    public void deleteOrderById(Long id) {
        softDelete(id);
    }

    @Override
    public void handlePayment(Long id, PaymentStatus paymentStatus){

        if (paymentStatus == PaymentStatus.PENDING) {
            return;
        }

        Order order = orderRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

        Status newStatus = switch (paymentStatus) {
            case SUCCESS -> Status.PAID;
            case FAILED -> Status.CANCELLED;
            default -> throw new IllegalStateException("Unsupported payment status: " + paymentStatus);
        };

        if (order.getStatus() == newStatus) {
            return;
        }

        order.setStatus(newStatus);
        orderRepository.save(order);
        
    }

    private void softDelete(Long id){
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new OrderNotFoundException("Order not found")
        );
        order.setDeleted(true);
        orderRepository.save(order);
    }


    private OrderWithUserDto buildOrderWithUserDto(Order order){
        return new OrderWithUserDto(
                orderMapper.toDto(order),
                userServiceGateway.getUser(order.getUserId())
        );
    }
}
