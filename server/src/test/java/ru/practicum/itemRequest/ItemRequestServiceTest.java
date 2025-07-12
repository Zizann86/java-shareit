package ru.practicum.itemRequest;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dal.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dal.ItemRequestRepository;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;

    private ItemRequestService itemRequestService;

    private final Long userId = 1L;
    private final Long requestId = 1L;
    private final User user = new User(userId, "User", "user@example.com");
    private final CreateItemRequestDto createDto = new CreateItemRequestDto("Need a drill");
    private final ItemRequest request;
    private final ItemRequestDto expectedDto;

    public ItemRequestServiceTest() {
        request = new ItemRequest();
        request.setId(requestId);
        request.setDescription("Need a drill");
        request.setRequester(user);
        request.setCreatedTime(LocalDateTime.now());

        expectedDto = new ItemRequestDto(
                requestId,
                "Need a drill",
                new UserDto(userId, "User", "user@example.com"),
                request.getCreatedTime(),
                List.of()
        );
    }

    @BeforeEach
    void setUp() {
        itemRequestService = new ItemRequestServiceImpl(itemRepository, userRepository, itemRequestRepository);
    }

    @Test
    void createItemRequest_shouldCreateNewRequest() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ArgumentCaptor<ItemRequest> requestCaptor = ArgumentCaptor.forClass(ItemRequest.class);

        when(itemRequestRepository.save(requestCaptor.capture())).thenReturn(request);

        ItemRequestDto result = itemRequestService.createItemRequest(userId, createDto);

        ItemRequest savedRequest = requestCaptor.getValue();

        assertEquals("Need a drill", savedRequest.getDescription());
        assertEquals(user, savedRequest.getRequester());
        assertNotNull(savedRequest.getCreatedTime());

        assertEquals(requestId, result.getId());
        assertEquals("Need a drill", result.getDescription());
        assertEquals(userId, result.getRequester().getId());
    }

    @Test
    void createItemRequest_shouldThrowWhenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemRequestService.createItemRequest(userId, createDto));
    }

    @Test
    void getAllRequestsById_shouldReturnUserRequests() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findAllByRequesterIdOrderByCreatedTimeDesc(userId))
                .thenReturn(List.of(request));

        List<ItemRequestDto> result = itemRequestService.getAllRequestsById(userId);

        assertEquals(1, result.size());
        assertEquals(requestId, result.get(0).getId());
        assertEquals("Need a drill", result.get(0).getDescription());
        verify(itemRequestRepository).findAllByRequesterIdOrderByCreatedTimeDesc(userId);
    }

    @Test
    void getAllRequestsById_shouldThrowWhenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemRequestService.getAllRequestsById(userId));
    }

    @Test
    void findAll_shouldReturnPaginatedRequests() {
        int from = 0;
        int size = 10;
        Page<ItemRequest> page = new PageImpl<>(List.of(request));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findAll(PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "created"))))
                .thenReturn(page);

        List<ItemRequestDto> result = itemRequestService.findAll(userId, from, size);

        assertEquals(1, result.size());
        assertEquals(requestId, result.get(0).getId());
    }

    @Test
    void findAll_shouldThrowWhenInvalidPagination() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(ValidationException.class,
                () -> itemRequestService.findAll(userId, -1, 10));
        assertThrows(ValidationException.class,
                () -> itemRequestService.findAll(userId, 0, -1));
    }

    @Test
    void findById_shouldReturnRequest() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

        ItemRequestDto result = itemRequestService.findById(userId, requestId);

        assertEquals(requestId, result.getId());
        assertEquals("Need a drill", result.getDescription());
        assertEquals(userId, result.getRequester().getId());
    }

    @Test
    void findById_shouldThrowWhenRequestNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemRequestService.findById(userId, requestId));
    }

    @Test
    void findById_shouldThrowWhenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemRequestService.findById(userId, requestId));
    }

    @Test
    void findAll_shouldReturnRequestsWithItems() {
        int from = 0;
        int size = 10;

        Item item = new Item();
        item.setId(1L);
        item.setName("Drill");
        item.setDescription("Powerful drill");
        item.setAvailable(true);
        item.setOwner(user);
        item.setItemRequest(request);

        request.setItems(List.of(item));

        Page<ItemRequest> page = new PageImpl<>(List.of(request));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findAll(PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "created"))))
                .thenReturn(page);

        List<ItemRequestDto> result = itemRequestService.findAll(userId, from, size);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getItems().size());
        assertEquals("Drill", result.get(0).getItems().get(0).getName());
    }
}
