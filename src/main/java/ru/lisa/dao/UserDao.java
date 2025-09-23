package ru.lisa.dao;

import ru.lisa.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserDao {
//    CRUD

    Long save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    void update(User user);
    boolean delete(Long id);
}