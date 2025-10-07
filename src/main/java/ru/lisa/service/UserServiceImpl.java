package ru.lisa.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.lisa.entity.User;
import ru.lisa.repository.UserRepository;

import java.util.List;
import java.util.Optional;


@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
        logger.info("UserService инициализирован с Spring Data JPA");
    }

    @Override
    public Long createUser(String name, String email, Integer age) {
        logger.debug("Попытка создания пользователя: name={}, email={}, age={}", name, email, age);

        validateUserData(name, email, age);

        if (userRepository.findByEmail(email).isPresent()) {
            logger.warn("Попытка создания пользователя с существующим email: {}", email);
            throw new IllegalArgumentException("Пользователь с email '" + email + "' уже существует");
        }

        User newUser = new User(name, email, age);
        User savedUser = userRepository.save(newUser);

        logger.info("Создан новый пользователь: ID={}, email={}", savedUser.getId(), email);
        return savedUser.getId();
    }

    @Override
    public Optional<User> getUserById(Long id) {
        if (id == null || id <= 0) {
            logger.warn("Получен некорректный ID: {}", id);
            throw new IllegalArgumentException("ID должен быть положительным числом");
        }
        logger.debug("Запрос пользователя с ID: {}", id);
        return userRepository.findById(id);
    }

    @Override
    public List<User> getAllUsers() {
        logger.debug("Запрос списка всех пользователей");
        List<User> users = userRepository.findAll();
        logger.info("Получено {} пользователей", users.size());
        return users;
    }

    @Override
    public void updateUser(Long userId, String name, String email, Integer age) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Некорректный ID пользователя");
        }
        validateUserData(name, email, age);

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("Пользователь с ID {} не найден для обновления", userId);
                    return new IllegalArgumentException("Пользователь с ID " + userId + " не найден");
                });

        // Проверяем, меняется ли email
        if (!existingUser.getEmail().equals(email)) {
            if (userRepository.findByEmail(email).isPresent()) {
                logger.warn("Конфликт email: '{}' уже используется", email);
                throw new IllegalArgumentException("Пользователь с email '" + email + "' уже существует");
            }
        }

        existingUser.setName(name);
        existingUser.setEmail(email);
        existingUser.setAge(age);

        userRepository.save(existingUser);
        logger.info("Пользователь с ID {} успешно обновлён", userId);
    }

    @Override
    public boolean deleteUser(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Некорректный ID пользователя");
        }

        if (!userRepository.existsById(id)) {
            logger.warn("Попытка удаления несуществующего пользователя с ID: {}", id);
            return false;
        }

        userRepository.deleteById(id);
        logger.info("Пользователь с ID {} успешно удалён", id);
        return true;
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

        if (age == null) {
            throw new IllegalArgumentException("Возраст не может быть null");
        }
        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("Возраст должен быть в диапазоне от 0 до 150 лет");
        }
    }
}