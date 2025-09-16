package ru.practicum.itemRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.item.dal.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dal.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class ItemRequestRepositoryTest {

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User requester1;
    private User requester2;
    private ItemRequest request1;
    private ItemRequest request2;
    private ItemRequest request3;

    @BeforeEach
    void setUp() {
        requester1 = userRepository.save(new User("Requester1", "requester1@example.com"));
        requester2 = userRepository.save(new User("Requester2", "requester2@example.com"));

        request1 = new ItemRequest();
        request1.setDescription("Нужна дрель");
        request1.setRequester(requester1);
        request1.setCreatedTime(LocalDateTime.now().minusDays(2));
        itemRequestRepository.save(request1);

        request2 = new ItemRequest();
        request2.setDescription("Нужна отвертка");
        request2.setRequester(requester1);
        request2.setCreatedTime(LocalDateTime.now().minusDays(1));
        itemRequestRepository.save(request2);

        request3 = new ItemRequest();
        request3.setDescription("Нужен молоток");
        request3.setRequester(requester2);
        request3.setCreatedTime(LocalDateTime.now());
        itemRequestRepository.save(request3);

        Item item1 = new Item();
        item1.setName("Дрель");
        item1.setDescription("Мощная дрель");
        item1.setAvailable(true);
        item1.setOwner(requester2);
        item1.setItemRequest(request1);
        itemRepository.save(item1);

        Item item2 = new Item();
        item2.setName("Отвертка");
        item2.setDescription("Аккумуляторная отвертка");
        item2.setAvailable(true);
        item2.setOwner(requester2);
        item2.setItemRequest(request2);
        itemRepository.save(item2);
    }

    @Test
    void findAllByRequesterIdOrderByCreatedTimeDesc_shouldReturnRequestsForUser() {
        List<ItemRequest> result = itemRequestRepository.findAllByRequesterIdOrderByCreatedTimeDesc(requester1.getId());

        assertEquals(2, result.size());
        assertEquals("Нужна отвертка", result.get(0).getDescription()); // Самый новый первый
        assertEquals("Нужна дрель", result.get(1).getDescription());
    }

    @Test
    void findAllByRequesterIdOrderByCreatedTimeDesc_shouldReturnEmptyListForWrongUser() {
        List<ItemRequest> result = itemRequestRepository.findAllByRequesterIdOrderByCreatedTimeDesc(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findAllByRequesterIdOrderByCreatedTimeDesc_shouldNotReturnOtherUsersRequests() {
        List<ItemRequest> result = itemRequestRepository.findAllByRequesterIdOrderByCreatedTimeDesc(requester2.getId());

        assertEquals(1, result.size());
        assertEquals("Нужен молоток", result.get(0).getDescription());
    }
}
