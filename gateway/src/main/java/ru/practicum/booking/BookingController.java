package ru.practicum.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.booking.dto.BookingState;
import ru.practicum.booking.dto.CreateBookingDto;

import static ru.practicum.util.Constants.USER_ID_HEADER;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> createBooking(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestBody @Valid CreateBookingDto createBookingDto) {
        log.info("Получен HTTP-запрос на добавление бронирования: {}", createBookingDto);
        return bookingClient.create(userId, createBookingDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> updateStatusBooking(@RequestHeader(USER_ID_HEADER) Long userId,
                                                      @PathVariable("bookingId") Long bookingId,
                                                      @RequestParam(name = "approved", required = true) Boolean approved) {
        log.info("Получен HTTP-запрос на обновление бронирования");
        return bookingClient.updateStatusBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(@RequestHeader(USER_ID_HEADER) Long userId,
                                                 @PathVariable("bookingId") Long bookingId) {
        log.info("Получен HTTP-запрос на получение бронирования по id: {}", bookingId);
        return bookingClient.getBookingById(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllBookingsForBooker(@RequestHeader(USER_ID_HEADER) long userId,
                                                          @RequestParam(name = "state", defaultValue = "all") String stateParam) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        log.info("Get bookings for user {} with state {}", userId, state);
        return bookingClient.getAllBookingsForBooker(userId, state);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllBookingsForOwner(@RequestHeader(USER_ID_HEADER) Long userId,
                                                         @RequestParam(defaultValue = "ALL") BookingState state) {
        return bookingClient.getBookingsForOwner(userId, state);
    }
}
