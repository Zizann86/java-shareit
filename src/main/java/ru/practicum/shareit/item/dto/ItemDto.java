package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@AllArgsConstructor
@Data
public class ItemDto {
    private Long id;
    @NotBlank(message = "Имя не должно быть null или пустое")
    private String name;
    @NotBlank(message = "Описание не должно быть null или пустое")
    private String description;
    @NotNull(message = "Статус не должен быть null или пуст")
    private Boolean available;
    BookingDto lastBooking;
    BookingDto nextBooking;
    List<CommentDto> comments;
}
