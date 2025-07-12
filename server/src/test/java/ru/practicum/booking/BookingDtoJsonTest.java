package ru.practicum.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingDtoJsonTest {
    private final JacksonTester<BookingDto> json;

    @Test
    void testSerialize() throws Exception {
        UserDto booker = new UserDto(2L, "Booker", "booker@example.com");
        ItemDto item = new ItemDto(
                1L,
                "Test Item",
                "Test Description",
                true,
                null,
                null,
                null,
                null
        );

        LocalDateTime start = LocalDateTime.of(2023, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2023, 1, 5, 10, 0);

        BookingDto dto = new BookingDto(
                1L,
                start,
                end,
                item,
                booker,
                BookingStatus.APPROVED
        );

        JsonContent<BookingDto> result = json.write(dto);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).hasJsonPath("$.start");
        assertThat(result).hasJsonPath("$.end");
        assertThat(result).hasJsonPath("$.item");
        assertThat(result).hasJsonPath("$.booker");
        assertThat(result).hasJsonPath("$.status");

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start")
                .isEqualTo("2023-01-01T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end")
                .isEqualTo("2023-01-05T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.status")
                .isEqualTo("APPROVED");

        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.item.name")
                .isEqualTo("Test Item");

        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.booker.name")
                .isEqualTo("Booker");
    }

    @Test
    void testDeserialize() throws Exception {
        String jsonString = "{"
                + "\"id\": 1, "
                + "\"start\": \"2023-01-01T10:00:00\", "
                + "\"end\": \"2023-01-05T10:00:00\", "
                + "\"item\": {"
                + "\"id\": 1, "
                + "\"name\": \"Test Item\", "
                + "\"description\": \"Test Description\", "
                + "\"available\": true"
                + "}, "
                + "\"booker\": {"
                + "\"id\": 2, "
                + "\"name\": \"Booker\", "
                + "\"email\": \"booker@example.com\""
                + "}, "
                + "\"status\": \"APPROVED\""
                + "}";

        BookingDto dto = json.parse(jsonString).getObject();

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getStart())
                .isEqualTo(LocalDateTime.of(2023, 1, 1, 10, 0));
        assertThat(dto.getEnd())
                .isEqualTo(LocalDateTime.of(2023, 1, 5, 10, 0));
        assertThat(dto.getStatus()).isEqualTo(BookingStatus.APPROVED);

        assertThat(dto.getItem()).isNotNull();
        assertThat(dto.getItem().getId()).isEqualTo(1L);
        assertThat(dto.getItem().getName()).isEqualTo("Test Item");

        assertThat(dto.getBooker()).isNotNull();
        assertThat(dto.getBooker().getId()).isEqualTo(2L);
        assertThat(dto.getBooker().getName()).isEqualTo("Booker");
    }

    @Test
    void testDeserializeWithMissingFields() throws Exception {
        String jsonString = "{"
                + "\"id\": 1, "
                + "\"start\": \"2023-01-01T10:00:00\", "
                + "\"end\": \"2023-01-05T10:00:00\", "
                + "\"status\": \"APPROVED\""
                + "}";

        BookingDto dto = json.parse(jsonString).getObject();

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getStart())
                .isEqualTo(LocalDateTime.of(2023, 1, 1, 10, 0));
        assertThat(dto.getEnd())
                .isEqualTo(LocalDateTime.of(2023, 1, 5, 10, 0));
        assertThat(dto.getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(dto.getItem()).isNull();
        assertThat(dto.getBooker()).isNull();
    }
}
