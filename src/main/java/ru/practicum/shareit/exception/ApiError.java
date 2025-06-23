package ru.practicum.shareit.exception;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiError {
    String description;
    Integer errorCode;
}
