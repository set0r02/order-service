package com.innowise.orderservice.dto.output;

import java.io.Serializable;

public record UserDto(

        Long id,

        String email,

        String name,

        String surname

) implements Serializable {
}
