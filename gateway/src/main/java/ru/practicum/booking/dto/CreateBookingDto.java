package ru.practicum.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingDto {
    @NotNull(message = "Идентификатор вещи не должнен быть пустой")
    Long itemId;
    @NotNull(message = "Дата старта брони не должна быть пустой")
    @FutureOrPresent(message = "Дата старта брони не должна быть в прошлом")
    LocalDateTime start;
    @NotNull(message = "Дата окончания брони не должна быть пустой")
    @Future(message = "Дата окончания брони должна быть в будущем")
    LocalDateTime end;
}
