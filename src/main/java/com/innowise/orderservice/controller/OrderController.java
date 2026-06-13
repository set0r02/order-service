package com.innowise.orderservice.controller;

import com.innowise.orderservice.dto.input.OrderInputDto;
import com.innowise.orderservice.dto.input.OrderUpdateInputDto;
import com.innowise.orderservice.dto.output.OrderWithUserDto;
import com.innowise.orderservice.model.Status;
import com.innowise.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderWithUserDto> createOrder(@Valid @RequestBody OrderInputDto orderInputDto){
        OrderWithUserDto orderWithUserDto = orderService.createOrder(orderInputDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderWithUserDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderWithUserDto> getOrderById(@PathVariable Long id){
        OrderWithUserDto orderWithUserDto = orderService.getOrderById(id);
        return ResponseEntity.status(HttpStatus.OK).body(orderWithUserDto);
    }

    @GetMapping
    public ResponseEntity<Page<OrderWithUserDto>> getOrders(Pageable pageable,
                                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
                                                          @RequestParam(required = false)List<Status> statuses){
        return ResponseEntity.status(HttpStatus.OK).body(orderService.getOrders(pageable,from,to,statuses));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderWithUserDto>> getOrdersByUserId(@PathVariable Long userId){
        return ResponseEntity.status(HttpStatus.OK).body(orderService.getOrdersByUserId(userId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<OrderWithUserDto> updateOrderById(@PathVariable Long id,
                                                          @Valid @RequestBody OrderUpdateInputDto orderUpdateInputDto){
        return ResponseEntity.status(HttpStatus.OK).body(orderService.updateOrderById(id,orderUpdateInputDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<OrderWithUserDto> deleteOrderById(@PathVariable Long id){
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(orderService.deleteOrderById(id));
    }

}
