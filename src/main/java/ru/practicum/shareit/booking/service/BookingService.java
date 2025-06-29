package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.List;

public interface BookingService {

    BookingDto create(Long userId, CreateBookingDto createBookingDto);

    BookingDto updateStatusBooking(Long userId, Long bookingId, boolean approved);

    BookingDto getBookingById(Long userId, Long bookingId);

    List<BookingDto> getBookingsByState(Long userId, BookingState state);

    List<BookingDto> getBookingsForOwner(Long ownerId, BookingState state);
}

