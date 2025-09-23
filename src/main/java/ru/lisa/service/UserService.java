package ru.lisa.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lisa.dao.UserDao;
import ru.lisa.dao.UserDaoImpl;
import ru.lisa.entity.User;

import java.util.List;
import java.util.Optional;

public class UserService {
    // Инициализация логгера через SLF4J
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
        logger.info("UserService инициализирован");
    }

    public UserService() {
        this(new UserDaoImpl());
    }

    public Long createUser(String name, String email, Integer age) {
        logger.debug("Попытка создания пользователя: name={}, email={}, age={}", name, email, age);

        validateUserData(name, email, age);

        Optional<User> existingUser = userDao.findByEmail(email);
        if (existingUser.isPresent()) {
            logger.warn("Попытка создания пользователя с существующим email: {}", email);
            throw new IllegalArgumentException("Пользователь с email '" + email + "' уже существует");
        }

        User newUser = new User(name, email, age);
        Long userId = userDao.save(newUser);

        logger.info("Создан новый пользователь: ID={}, email={}", userId, email);
        return userId;
    }

    public Optional<User> getUserById(Long id) {
        logger.debug("Запрос пользователя с ID: {}", id);

        if (id == null || id <= 0) {
            logger.warn("Некорректный ID пользователя: {}", id);
            throw new IllegalArgumentException("ID пользователя должен быть положительным числом");
        }

        Optional<User> user = userDao.findById(id);

        if (user.isPresent()) {
            logger.debug("Пользователь с ID {} найден", id);
        } else {
            logger.debug("Пользователь с ID {} не найден", id);
        }

        return user;
    }

    public List<User> getAllUsers() {
        logger.debug("Запрос списка всех пользователей");

        List<User> users = userDao.findAll();
        logger.info("Получено {} пользователей", users.size());

        return users;
    }

    public void updateUser(Long userId, String name, String email, Integer age) {
        logger.debug("Обновление пользователя ID={}, name={}, email={}, age={}",
                userId, name, email, age);

        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Некорректный ID пользователя");
        }

        validateUserData(name, email, age);

        Optional<User> userOpt = userDao.findById(userId);
        if (userOpt.isEmpty()) {
            logger.error("Пользователь с ID {} не найден для обновления", userId);
            throw new IllegalArgumentException("Пользователь с ID " + userId + " не найден");
        }

        User user = userOpt.get();

        if (!user.getEmail().equals(email)) {
            Optional<User> userWithSameEmail = userDao.findByEmail(email);
            if (userWithSameEmail.isPresent()) {
                logger.warn("Конфликт email при обновлении: новый email {} уже существует", email);
                throw new IllegalArgumentException("Пользователь с email '" + email + "' уже существует");
            }
        }

        user.setName(name);
        user.setEmail(email);
        user.setAge(age);

        userDao.update(user);
        logger.info("Пользователь с ID {} успешно обновлен", userId);
    }

    public boolean deleteUser(Long id) {
        logger.debug("Попытка удаления пользователя с ID: {}", id);

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Некорректный ID пользователя");
        }

        boolean isDeleted = userDao.delete(id);

        if (isDeleted) {
            logger.info("Пользователь с ID {} успешно удален", id);
        } else {
            logger.warn("Пользователь с ID {} не найден для удаления", id);
        }

        return isDeleted;
    }

    public Optional<User> getUserByEmail(String email) {
        logger.debug("Поиск пользователя по email: {}", email);

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email не может быть пустым");
        }

        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Некорректный формат email: " + email);
        }

        return userDao.findByEmail(email.trim());
    }

    public long getUsersCount() {
        logger.debug("Запрос количества пользователей");

        List<User> users = userDao.findAll();
        long count = users.size();
        logger.debug("Текущее количество пользователей: {}", count);

        return count;
    }

    private void validateUserData(String name, String email, Integer age) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя не может быть пустым");
        }
        if (name.trim().length() > 100) {
            throw new IllegalArgumentException("Имя не может превышать 100 символов");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email не может быть пустым");
        }
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Некорректный формат email: " + email);
        }

        if (age == null) {
            throw new IllegalArgumentException("Возраст не может быть null");
        }
        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("Возраст должен быть от 0 до 150 лет");
        }
    }

    private boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        return email.contains("@") && email.contains(".");
    }

    public void close() {
        logger.info("Закрытие ресурсов UserService");
    }
}