package com.innowise.orderservice.dto.output;

import java.io.Serializable;

public record UserOutputDto(

        Long id,

        String email,

        String name,

        String surname

) implements Serializable {
}
