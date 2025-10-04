package ru.lisa.service;

import ru.lisa.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    Long createUser(String name, String email, Integer age);

    Optional<User> getUserById(Long id);

    List<User> getAllUsers();

    void updateUser(Long userId, String name, String email, Integer age);

    boolean deleteUser(Long id);
}
