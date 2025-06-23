package ru.practicum.shareit.user.dal;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Repository
public class UserRepositorylmpl implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private Long idCounterUser = 1L;

    @Override
    public User createUser(User user) {
        user.setId(idCounterUser++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> getUser(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User updateUser(User user) {
        User updateUser = users.get(user.getId());

        if (user.getName() != null) {
            updateUser.setName(user.getName());
        }
        if (user.getEmail() != null) {
            updateUser.setEmail(user.getEmail());
        }
        return updateUser;
    }

    @Override
    public void deleteUser(Long userId) {
        users.remove(userId);
    }
}
