package com.innowise.orderservice.dto.output;

public record OrderWithUserDto(
        OrderOutputDto orderOutputDto,
        UserDto userDto
) {
}
