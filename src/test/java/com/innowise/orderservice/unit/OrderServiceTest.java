package com.innowise.orderservice.unit;


import com.innowise.orderservice.client.service.UserServiceGateway;
import com.innowise.orderservice.dto.input.OrderInputDto;
import com.innowise.orderservice.dto.input.OrderItemInputDto;
import com.innowise.orderservice.dto.input.OrderUpdateInputDto;
import com.innowise.orderservice.dto.output.OrderOutputDto;
import com.innowise.orderservice.dto.output.OrderWithUserDto;
import com.innowise.orderservice.dto.output.UserDto;
import com.innowise.orderservice.exception.OrderNotFoundException;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.model.Item;
import com.innowise.orderservice.model.Order;
import com.innowise.orderservice.model.enums.PaymentStatus;
import com.innowise.orderservice.model.enums.Status;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private UserServiceGateway userServiceGateway;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createOrderTest() {
        OrderItemInputDto itemInput = new OrderItemInputDto(1L, 2);

        OrderInputDto input = new OrderInputDto(
                10L,
                List.of(itemInput)
        );

        Item item = new Item();
        item.setId(1L);
        item.setPrice(BigDecimal.valueOf(100));

        Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setUserId(10L);

        OrderOutputDto outputDto = mock(OrderOutputDto.class);
        UserDto userDto = new UserDto(1L, "test@example.com", "John", "Doe");

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderMapper.toDto(savedOrder)).thenReturn(outputDto);
        when(userServiceGateway.getUser(10L)).thenReturn(userDto);

        OrderWithUserDto result = orderService.createOrder(input);

        assertNotNull(result);
        verify(itemRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(userServiceGateway, times(1)).getUser(10L);
    }

    @Test
    void getOrderByIdTest() {
        Long id = 1L;
        Long userId = 10L;

        Order order = new Order();
        order.setId(id);
        order.setUserId(userId);
        order.setDeleted(false);

        OrderOutputDto orderOutputDto = mock(OrderOutputDto.class);

        UserDto userDto = new UserDto(userId, "test@example.com", "John", "Doe");

        when(orderRepository.findByIdAndDeletedFalse(id))
                .thenReturn(Optional.of(order));

        when(orderMapper.toDto(order)).thenReturn(orderOutputDto);
        when(userServiceGateway.getUser(userId)).thenReturn(userDto);

        OrderWithUserDto result = orderService.getOrderById(id);

        assertNotNull(result);

        verify(orderRepository).findByIdAndDeletedFalse(id);
        verify(orderMapper).toDto(order);
        verify(userServiceGateway).getUser(userId);
    }

    @Test
    void getOrdersTest() {
        Pageable pageable = Pageable.ofSize(10);

        Order order = new Order();
        order.setUserId(10L);

        Page<Order> page = new PageImpl<>(List.of(order));

        OrderOutputDto dto = mock(OrderOutputDto.class);
        UserDto userDto = new UserDto(1L, "test@example.com", "John", "Doe");

        when(orderRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);
        when(orderMapper.toDto(order)).thenReturn(dto);
        when(userServiceGateway.getUser(10L)).thenReturn(userDto);

        Page<OrderWithUserDto> result = orderService.getOrders(pageable, LocalDateTime.now().minusDays(1), LocalDateTime.now(), List.of(Status.CREATED));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(orderRepository).findAll(any(Specification.class), eq(pageable));
        verify(userServiceGateway).getUser(10L);
    }

    @Test
    void getOrdersByUserIdTest() {
        Long userId = 1L;

        Order order = new Order();
        OrderOutputDto dto = mock(OrderOutputDto.class);
        UserDto userDto = new UserDto(1L, "test@example.com", "John", "Doe");

        when(orderRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(List.of(order));
        when(orderMapper.toDto(order)).thenReturn(dto);
        when(userServiceGateway.getUser(userId)).thenReturn(userDto);

        List<OrderWithUserDto> result = orderService.getOrdersByUserId(userId);

        assertEquals(1, result.size());
        verify(orderRepository).findByUserIdAndDeletedFalse(userId);
        verify(userServiceGateway).getUser(userId);
    }

    @Test
    void updateOrderByIdTest() {
        Long id = 1L;

        Order order = new Order();
        order.setId(id);
        order.setUserId(10L);

        OrderUpdateInputDto updateDto = mock(OrderUpdateInputDto.class);

        OrderOutputDto dto = mock(OrderOutputDto.class);
        UserDto userDto = new UserDto(1L, "test@example.com", "John", "Doe");

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(dto);
        when(userServiceGateway.getUser(10L)).thenReturn(userDto);
        when(updateDto.status()).thenReturn(Status.SHIPPED);

        OrderWithUserDto result = orderService.updateOrderById(id, updateDto);

        assertNotNull(result);
        assertEquals(Status.SHIPPED, order.getStatus());
        verify(userServiceGateway).getUser(10L);
    }

    @Test
    void deleteOrderTest() {
        Long id = 1L;

        Order order = new Order();
        order.setId(id);
        order.setUserId(10L);
        order.setDeleted(false);

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.deleteOrderById(id);

        assertTrue(order.isDeleted());

        verify(orderRepository).findById(id);
        verify(orderRepository).save(order);
    }

    @Test
    void handlePayment_success_shouldMarkOrderAsPaid() {
        Long orderId = 1L;

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(Status.CREATED);

        when(orderRepository.findByIdAndDeletedFalse(orderId))
                .thenReturn(Optional.of(order));

        orderService.handlePayment(orderId, PaymentStatus.SUCCESS);

        assertEquals(Status.PAID, order.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void handlePayment_failed_shouldMarkOrderAsCancelled() {
        Long orderId = 2L;

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(Status.CREATED);

        when(orderRepository.findByIdAndDeletedFalse(orderId))
                .thenReturn(Optional.of(order));

        orderService.handlePayment(orderId, PaymentStatus.FAILED);

        assertEquals(Status.CANCELLED, order.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void handlePayment_pending_shouldDoNothing() {
        Long orderId = 3L;

        orderService.handlePayment(orderId, PaymentStatus.PENDING);

        verifyNoInteractions(orderRepository);
    }

    @Test
    void handlePayment_orderNotFound_shouldThrowException() {
        Long orderId = 999L;

        when(orderRepository.findByIdAndDeletedFalse(orderId))
                .thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> orderService.handlePayment(orderId, PaymentStatus.SUCCESS));

        verify(orderRepository, never()).save(any());
    }
}
