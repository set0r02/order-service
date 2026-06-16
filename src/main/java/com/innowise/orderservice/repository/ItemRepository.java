package com.innowise.orderservice.repository;

import com.innowise.orderservice.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item,Long> {

    Optional<Item> findById(Long id);

}
