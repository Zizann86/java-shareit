package ru.practicum.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingService bookingService;

    private final Long userId = 1L;
    private final Long bookingId = 1L;
    private final Long itemId = 1L;

    private final LocalDateTime start = LocalDateTime.now().plusDays(1);
    private final LocalDateTime end = LocalDateTime.now().plusDays(2);

    private final CreateBookingDto createBookingDto = new CreateBookingDto(itemId, start, end);

    private final UserDto userDto = new UserDto(1L, "User", "user@example.com");
    private final ItemDto itemDto = new ItemDto(
            itemId, "Item", "Description", true, null, null, null, null);
    private final BookingDto bookingDto = new BookingDto(
            bookingId,
            start.minusHours(1),
            end.minusHours(1),
            itemDto,
            userDto,
            BookingStatus.WAITING);

    @Test
    void createBooking_shouldReturnCreatedBooking() throws Exception {
        when(bookingService.create(userId, createBookingDto)).thenReturn(bookingDto);

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(createBookingDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.booker.id").value(1L))
                .andExpect(jsonPath("$.item.name").value("Item"));

        verify(bookingService).create(userId, createBookingDto);
    }

    @Test
    void updateStatusBooking_shouldReturnUpdatedBooking() throws Exception {
        BookingDto approvedBooking = new BookingDto(
                bookingId,
                start.minusHours(1),
                end.minusHours(1),
                itemDto,
                userDto,
                BookingStatus.APPROVED);

        when(bookingService.updateStatusBooking(userId, bookingId, true)).thenReturn(approvedBooking);

        mvc.perform(patch("/bookings/{bookingId}?approved=true", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(bookingService).updateStatusBooking(userId, bookingId, true);
    }

    @Test
    void getBookingById_shouldReturnBooking() throws Exception {
        when(bookingService.getBookingById(userId, bookingId)).thenReturn(bookingDto);

        mvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.item.name").value("Item"));

        verify(bookingService).getBookingById(userId, bookingId);
    }

    @Test
    void getAllBookingsForBooker_shouldReturnBookingsList() throws Exception {
        when(bookingService.getBookingsByState(userId, BookingState.ALL))
                .thenReturn(List.of(bookingDto));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookingId))
                .andExpect(jsonPath("$[0].status").value("WAITING"));

        verify(bookingService).getBookingsByState(userId, BookingState.ALL);
    }

    @Test
    void getAllBookingsForOwner_shouldReturnBookingsList() throws Exception {
        when(bookingService.getBookingsForOwner(userId, BookingState.ALL))
                .thenReturn(List.of(bookingDto));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookingId))
                .andExpect(jsonPath("$[0].item.name").value("Item"));

        verify(bookingService).getBookingsForOwner(userId, BookingState.ALL);
    }
}
