package com.innowise.orderservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "status", nullable = false)
    private boolean status;

    @Column(name = "total_price", nullable = false)
    private double totalPrice;

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    @Builder.Default
    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems = new ArrayList<>();

}
