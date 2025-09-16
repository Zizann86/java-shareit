package ru.practicum.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.booking.dto.BookingDto;

import java.util.List;

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
    private Long requestId;
    BookingDto lastBooking;
    BookingDto nextBooking;
    List<CommentDto> comments;
}
