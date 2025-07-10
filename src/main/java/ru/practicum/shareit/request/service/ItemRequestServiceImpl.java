package ru.practicum.shareit.request.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dal.ItemRepository;
import ru.practicum.shareit.request.dal.ItemRequestRepository;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    public ItemRequestDto createItemRequest(Long userId, CreateItemRequestDto createItemRequestDto) {
       /* validateUserExist(userId);
        ItemRequest request = ItemRequestMapper.toItemRequest(createItemRequestDto);
        ItemRequest savedRequest = itemRequestRepository.save(request);
        return ItemRequestMapper.toItemRequestDto(savedRequest);*/
        User requester = validateUserExist(userId);  // Получаем пользователя
        ItemRequest request = ItemRequestMapper.toItemRequest(createItemRequestDto);
        request.setRequester(requester);
        request.setCreatedTime(LocalDateTime.now());
        ItemRequest savedRequest = itemRequestRepository.save(request);
        return ItemRequestMapper.toItemRequestDto(savedRequest);
    }

    @Override
    public List<ItemRequestDto> getAllRequestsById(Long userId) {
        validateUserExist(userId);
        return itemRequestRepository.findAllByRequesterIdOrderByCreatedTimeDesc(userId)
                .stream()
                .map(request -> {
                    ItemRequestDto requestDto = ItemRequestMapper.toItemRequestDto(request);
                    return requestDto;
                })
                .toList();
    }

    @Override
    public List<ItemRequestDto> findAll(Long userId, Integer from, Integer size) {
        validateUserExist(userId);
        if (from < 0 || size < 0) {
            throw new ValidationException("Аргументы не могут быть отрицательными.");
        }
        return itemRequestRepository.findAll(PageRequest.of((from / size), size,
                        Sort.by(Sort.Direction.DESC, "created")))
                .stream()
                .map(request -> {
                    ItemRequestDto requestDto = ItemRequestMapper.toItemRequestDto(request);
                    return requestDto;
                })
                .toList();
    }

    @Override
    public ItemRequestDto findById(Long userId, Long requestId) {
        validateUserExist(userId);
        ItemRequestDto requestDto = ItemRequestMapper.toItemRequestDto(itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(String.format("Запрос с ID = %d не найден!", requestId))));
        return requestDto;
    }

    private User validateUserExist(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id %d не найден.", userId)));
    }
}
