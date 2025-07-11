package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CreateBookingDto {
    Long itemId;
    LocalDateTime start;
    LocalDateTime end;
}
