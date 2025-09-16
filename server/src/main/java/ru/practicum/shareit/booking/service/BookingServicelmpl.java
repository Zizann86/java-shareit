package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dal.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dal.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServicelmpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public BookingDto create(Long userId, CreateBookingDto createBookingDto) {
        User booker = validateUserExist(userId);
        Item item = validateItemExist(createBookingDto.getItemId());
        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Нельзя забронировать свою собственную вещь");
        }
        if (!item.getAvailable()) {
            throw new IllegalStateException("Данная вещь не доступна!");
        }
        Booking booking = BookingMapper.mapToCreateBooking(createBookingDto);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto updateStatusBooking(Long userId, Long bookingId, boolean approved) {
        Booking booking = validateBookingExist(bookingId);
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new IllegalStateException("Нельзя изменить статус уже подтвержденного или отклоненного бронирования");
        }
        Item item = booking.getItem();
        if (!item.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Данная вещь не принадлежит этому пользователю");
        }

        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);

        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }

        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto getBookingById(Long userId, Long bookingId) {
        Booking booking = validateBookingExist(bookingId);

        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Бронирование не связано с данным пользователем");
        }
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getBookingsByState(Long bookerId, BookingState state) {
        validateUserExist(bookerId);

        List<Booking> bookings;
        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case BookingState.WAITING: {
                bookings = new ArrayList<>(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(bookerId,
                        BookingStatus.WAITING));
                break;
            }
            case BookingState.REJECTED: {
                bookings = new ArrayList<>(bookingRepository.findAllByBookerIdAndStatusInOrderByStartDesc(bookerId,
                        List.of(BookingStatus.REJECTED, BookingStatus.CANCELED)));
                break;
            }
            case BookingState.CURRENT: {
                bookings = new ArrayList<>(bookingRepository.findCurrentBookings(bookerId));
                break;
            }
            case BookingState.FUTURE: {
                bookings = new ArrayList<>(bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(bookerId,
                        now));
                break;
            }
            case BookingState.PAST: {
                bookings = new ArrayList<>(bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(bookerId,
                        now));
                break;
            }
            case BookingState.ALL: {
                bookings = new ArrayList<>(bookingRepository.findAllByBookerIdOrderByStartDesc(bookerId));
                break;
            }
            default:
                bookings = new ArrayList<>();
        }
        return bookings.stream().map(BookingMapper::toBookingDto).toList();
    }

    @Override
    public List<BookingDto> getBookingsForOwner(Long ownerId, BookingState state) {
        validateUserExist(ownerId);

        List<Item> userItemsIds = itemRepository.findAllByOwnerId(ownerId);

        if (userItemsIds.isEmpty()) {
            throw new NotFoundException("Этот запрос только для тех пользователей, которые имеют хотя бы 1 вещь");
        }

        List<Booking> bookings;
        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case BookingState.WAITING: {
                bookings = new ArrayList<>(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(ownerId,
                        BookingStatus.WAITING));
                break;
            }
            case BookingState.REJECTED: {
                bookings = new ArrayList<>(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(ownerId,
                        BookingStatus.REJECTED));
                break;
            }
            case BookingState.CURRENT: {
                bookings = new ArrayList<>(bookingRepository.findCurrentBookingsByOwner(ownerId));
                break;
            }
            case BookingState.FUTURE: {
                bookings = new ArrayList<>(bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(ownerId,
                        now));
                break;
            }
            case BookingState.PAST: {
                bookings = new ArrayList<>(bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(ownerId,
                        now));
                break;
            }
            case BookingState.ALL: {
                bookings = new ArrayList<>(bookingRepository.findAllByItemOwnerIdOrderByStartDesc(ownerId));
                break;
            }
            default:
                bookings = new ArrayList<>();
        }
        return bookings.stream().map(BookingMapper::toBookingDto).toList();
    }

    private Item validateItemExist(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Предмет с id %d не найден.", itemId)));
    }

    private User validateUserExist(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id %d не найден.", userId)));
    }

    private Booking validateBookingExist(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Бронирование с id %d не найдено.", id)));
    }
}
