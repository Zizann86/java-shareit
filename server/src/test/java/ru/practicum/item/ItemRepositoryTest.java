package ru.practicum.item;

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


import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private User owner;
    private User requester;
    private ItemRequest request;
    private Item item1;
    private Item item2;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(new User("Owner", "owner@example.com"));
        requester = userRepository.save(new User("Requester", "requester@example.com"));

        request = new ItemRequest();
        request.setDescription("Нужна дрель");
        request.setRequester(requester);
        request = itemRequestRepository.save(request);

        item1 = new Item();
        item1.setName("Дрель");
        item1.setDescription("Мощная дрель");
        item1.setAvailable(true);
        item1.setOwner(owner);
        item1.setItemRequest(null);
        itemRepository.save(item1);

        item2 = new Item();
        item2.setName("Аккумуляторная отвертка");
        item2.setDescription("Отвертка с батареей");
        item2.setAvailable(true);
        item2.setOwner(owner);
        item2.setItemRequest(request);
        itemRepository.save(item2);
    }

    @Test
    void findAllByOwnerId_shouldReturnOwnerItems() {
        List<Item> result = itemRepository.findAllByOwnerId(owner.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(i -> i.getName().equals("Дрель")));
        assertTrue(result.stream().anyMatch(i -> i.getName().equals("Аккумуляторная отвертка")));
    }

    @Test
    void search_shouldFindAvailableItemsByText() {
        List<Item> result = itemRepository.search("дрель");

        assertEquals(1, result.size());
        assertEquals("Дрель", result.get(0).getName());
    }

    @Test
    void search_shouldNotFindUnavailableItems() {
        Item unavailableItem = new Item();
        unavailableItem.setName("Сломанная дрель");
        unavailableItem.setDescription("Не работает");
        unavailableItem.setAvailable(false);
        unavailableItem.setOwner(owner);
        itemRepository.save(unavailableItem);

        List<Item> result = itemRepository.search("дрель");

        assertEquals(1, result.size()); // Находит только доступную дрель
    }

    @Test
    void search_shouldBeCaseInsensitive() {
        List<Item> result = itemRepository.search("ДРеЛь");

        assertEquals(1, result.size());
        assertEquals("Дрель", result.get(0).getName());
    }

    @Test
    void findByItemRequestIdOrderByIdDesc_shouldReturnItemsForRequest() {
        List<Item> result = itemRepository.findByItemRequestIdOrderByIdDesc(request.getId());

        assertEquals(1, result.size());
        assertEquals("Аккумуляторная отвертка", result.get(0).getName());
    }

    @Test
    void findByItemRequestIdOrderByIdDesc_shouldReturnEmptyListForWrongRequest() {
        List<Item> result = itemRepository.findByItemRequestIdOrderByIdDesc(999L);

        assertTrue(result.isEmpty());
    }
}
