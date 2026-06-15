package com.innowise.orderservice.specifications;

import com.innowise.orderservice.model.Order;
import com.innowise.orderservice.model.Status;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

@UtilityClass
public class OrderSpecifications {
    public static Specification<Order> hasStatuses(List<Status> statusList) {
        return (root, query, criteriaBuilder) ->
            statusList == null || statusList.isEmpty() ? null : root.get("status").in(statusList);
    }
    public static Specification<Order> createdBetween(LocalDateTime from, LocalDateTime to){
        return ((root, query, criteriaBuilder) -> {
            if(from == null && to == null) return null;
            if(from == null) return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"),to);
            if(to == null) return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"),from);
            return criteriaBuilder.between(root.get("createdAt"),from,to);
        });
    }

    public static Specification<Order> notDeleted(){
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.isFalse(root.get("deleted"))
        );
    }
}

