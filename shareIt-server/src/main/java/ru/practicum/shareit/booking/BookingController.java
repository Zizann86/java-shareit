package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingDto createBooking(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody @Valid CreateBookingDto createBookingDto) {
        log.info("Получен HTTP-запрос на добавление бронирования: {}", createBookingDto);
        return bookingService.create(userId, createBookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto updateStatusBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                          @PathVariable("bookingId") Long bookingId,
                                          @RequestParam(name = "approved", required = true) Boolean approved) {
        log.info("Получен HTTP-запрос на обновление бронирования");
        return bookingService.updateStatusBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                     @PathVariable("bookingId") Long bookingId) {
        log.info("Получен HTTP-запрос на получение бронирования по id: {}", bookingId);
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getAllBookingsForBooker(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                    @RequestParam(defaultValue = "ALL") BookingState state) {
        log.info("Получен HTTP-запрос на получение всех броней у пользователя с id: {}", userId);
        return bookingService.getBookingsByState(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllBookingsForOwner(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                   @RequestParam(defaultValue = "ALL") BookingState state) {
        return bookingService.getBookingsForOwner(userId, state);
    }
}
