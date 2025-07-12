package ru.practicum.itemRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemRequestService itemRequestService;

    private final Long userId = 1L;
    private final Long requestId = 1L;
    private final LocalDateTime created = LocalDateTime.now();

    private final CreateItemRequestDto createItemRequestDto = new CreateItemRequestDto("Need a drill");
    private final UserDto requesterDto = new UserDto(userId, "Requester", "requester@example.com");
    private final ItemDto itemDto = new ItemDto(1L, "Drill", "Powerful drill", true, null, null, null, null);
    private final ItemRequestDto itemRequestDto;

    public ItemRequestControllerTest() {
        itemRequestDto = new ItemRequestDto(
                requestId,
                "Need a drill",
                requesterDto,
                created,
                List.of(itemDto));
    }

    @Test
    void create_shouldReturnCreatedRequest() throws Exception {
        when(itemRequestService.createItemRequest(userId, createItemRequestDto))
                .thenReturn(itemRequestDto);

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(createItemRequestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.description").value("Need a drill"))
                .andExpect(jsonPath("$.requester.id").value(userId))
                .andExpect(jsonPath("$.items[0].name").value("Drill"));

        verify(itemRequestService).createItemRequest(userId, createItemRequestDto);
    }

    @Test
    void getAllRequestsById_shouldReturnRequestsList() throws Exception {
        when(itemRequestService.getAllRequestsById(userId))
                .thenReturn(List.of(itemRequestDto));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(requestId))
                .andExpect(jsonPath("$[0].requester.name").value("Requester"))
                .andExpect(jsonPath("$[0].items[0].name").value("Drill"));

        verify(itemRequestService).getAllRequestsById(userId);
    }

    @Test
    void getAllRequests_shouldReturnPaginatedRequests() throws Exception {
        when(itemRequestService.findAll(userId, 0, 50))
                .thenReturn(List.of(itemRequestDto));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", "0")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(requestId))
                .andExpect(jsonPath("$[0].description").value("Need a drill"))
                .andExpect(jsonPath("$[0].created").exists());

        verify(itemRequestService).findAll(userId, 0, 50);
    }

    @Test
    void getRequestById_shouldReturnRequest() throws Exception {
        when(itemRequestService.findById(userId, requestId))
                .thenReturn(itemRequestDto);

        mvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.requester.email").value("requester@example.com"))
                .andExpect(jsonPath("$.items[0].description").value("Powerful drill"));

        verify(itemRequestService).findById(userId, requestId);
    }
}
