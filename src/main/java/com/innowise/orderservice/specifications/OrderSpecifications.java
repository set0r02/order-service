package com.innowise.orderservice.specifications;

import com.innowise.orderservice.model.Order;
import com.innowise.orderservice.model.Status;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

public class OrderSpecifications {
    public static Specification<Order> hasStatuses(List<Status> statusList) {
        return (root, query, criteriaBuilder) ->
            statusList == null || statusList.isEmpty() ? null : root.get("status").in(statusList);
    }
    public static Specification<Order> createdBetween(LocalDateTime start, LocalDateTime end){
        return ((root, query, criteriaBuilder) -> {
            if(start == null && end == null) return null;
            if(start == null) return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"),end);
            if(end == null) return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"),start);
            return criteriaBuilder.between(root.get("createdAt"),start,end);
        });
    }
}

