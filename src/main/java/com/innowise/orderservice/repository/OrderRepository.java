package com.innowise.orderservice.repository;

import com.innowise.orderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long>, JpaSpecificationExecutor<Order> {

    List<Order> findByUserId(Long userId);

    /*@Modifying
    @Query("update Order o set o.deleted = false where o.id = :id")
    Order softDelete(@Param("id") Long id);*/
}
