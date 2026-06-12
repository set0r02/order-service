package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.dto.input.OrderInputDto;
import com.innowise.orderservice.dto.input.OrderItemInputDto;
import com.innowise.orderservice.dto.output.OrderOutputDto;
import com.innowise.orderservice.exception.NotFoundException;
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
import org.aspectj.weaver.ast.Or;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;

    @Override
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

    @Override
    public OrderOutputDto getOrderById(Long id){
        Order order = orderRepository.findById(id)
                .filter(ord -> !ord.isDeleted())
                .orElseThrow(() ->
                        new NotFoundException("Order not found"));

        return orderMapper.toDto(order);
    }

    @Override
    public Page<OrderOutputDto> getOrders(Pageable pageable, LocalDateTime from, LocalDateTime to, List<Status> statuses) {
        Specification<Order> specification = Specification.where(OrderSpecifications.createdBetween(from,to))
                .and(OrderSpecifications.hasStatuses(statuses));
        return orderRepository.findAll(specification,pageable)
                .map(orderMapper::toDto);
    }

    @Override
    public List<OrderOutputDto> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(orderMapper::toDto).toList();
    }

    @Override
    public OrderOutputDto updateOrderById(Long id, OrderInputDto orderInputDto) {
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Order not found")
        );
        return orderMapper.toDto(orderRepository.save());
    }

    @Override
    public OrderOutputDto deleteOrderById(Long id) {
        return orderMapper.toDto(softDelete(id));
    }

    private Order softDelete(Long id){
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Order not found")
        );
        order.setDeleted(true);
        return orderRepository.save(order);
    }

}
