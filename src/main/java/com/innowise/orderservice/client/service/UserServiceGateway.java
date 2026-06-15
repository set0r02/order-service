package com.innowise.orderservice.client.service;

import com.innowise.orderservice.client.UserServiceClient;
import com.innowise.orderservice.dto.output.UserDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceGateway {

    private final UserServiceClient userServiceClient;

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackUser")
    public UserDto getUser(Long userId){
        return userServiceClient.getUserById(userId);
    }

    public UserDto fallbackUser(Long userId,Throwable throwable){

        log.warn("User service unavailable for userId {}", userId, throwable);

        return new UserDto(userId,
                "unknown@gmail.com",
                "Unknown",
                "Unknown");
    }

}
