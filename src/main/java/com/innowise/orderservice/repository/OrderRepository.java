package com.innowise.orderservice.repository;

import com.innowise.orderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {

    @Override
    Optional<Order> findById(Long aLong);
}
