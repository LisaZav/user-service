package ru.lisa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.lisa.entity.User;
import ru.lisa.kafka.UserEventProducer;
import ru.lisa.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserEventProducer producer;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Проверка создания пользователя и отправки события в Kafka")
    void testCreateUser() {
        // given
        String name = "Leon Fix";
        String email = "leon@rambler.com";
        Integer age = 24;

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        User savedUser = new User(name, email, age);
        savedUser.setId(1L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // when
        Long actualId = userService.createUser(name, email, age);

        // then
        assertEquals(1L, actualId);
        verify(userRepository).findByEmail(email);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Создание пользователя с существующим email должно выбрасывать исключение и не отправлять в Kafka")
    void testCreateUserWithExistingEmail() {
        String email = "leon@rambler.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser("Leon", email, 24)
        );

        assertEquals("Пользователь с email 'leon@rambler.com' уже существует", exception.getMessage());
        verify(userRepository, never()).save(any());
    }


    @Test
    @DisplayName("Проверка удаления пользователя и отправки события в Kafka")
    void testDeleteUser() {
        long id = 1L;
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        boolean actual = userService.deleteUser(id);

        assertTrue(actual);
        verify(userRepository).findById(id);
        verify(userRepository).deleteById(id);

    }

    @Test
    @DisplayName("Удаление несуществующего пользователя должно возвращать false и не отправлять в Kafka")
    void testDeleteUserNotFound() {
        long id = 999L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        boolean actual = userService.deleteUser(id);

        assertFalse(actual);
        verify(userRepository).findById(id);
        verify(userRepository, never()).deleteById(anyLong());
    }


    @Test
    @DisplayName("Обновление пользователя без изменения email")
    void testUpdateUserWithoutEmailChange() {
        Long userId = 23L;
        String email = "old@example.com";
        User existingUser = new User("Old Name", email, 20);
        existingUser.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        userService.updateUser(userId, "New Name", email, 30);

        verify(userRepository).findById(userId);
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository).save(existingUser);
        assertEquals("New Name", existingUser.getName());
        assertEquals(30, existingUser.getAge());
    }

    @Test
    @DisplayName("Обновление с конфликтом email должно выбрасывать исключение")
    void testUpdateUserWithConflictingEmail() {
        Long userId = 1L;
        User existingUser = new User("User1", "user1@example.com", 25);
        existingUser.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail("user2@example.com")).thenReturn(Optional.of(new User()));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(userId, "New Name", "user2@example.com", 30)
        );

        assertEquals("Пользователь с email 'user2@example.com' уже существует", ex.getMessage());
        verify(userRepository, never()).save(any());
    }


    @Test
    @DisplayName("Получение пользователя по валидному ID")
    void testGetUserByIdWithValidId() {
        Long userId = 1L;
        User user = new User("Leon Fix", "leon@rambler.com", 24);
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Optional<User> actual = userService.getUserById(userId);

        assertTrue(actual.isPresent());
        assertEquals(userId, actual.get().getId());
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Получение пользователя по отрицательному ID должно выбрасывать исключение")
    void testGetUserByIdWithNegativeId() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.getUserById(-1L)
        );
        assertEquals("ID должен быть положительным числом", ex.getMessage());
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Поиск всех пользователей")
    void testFindAll() {
        User user1 = new User("Alice", "alice@example.com", 30);
        User user2 = new User("Bob", "bob@example.com", 25);
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<User> allUsers = userService.getAllUsers();

        assertEquals(2, allUsers.size());
        assertEquals("Alice", allUsers.get(0).getName());
        assertEquals("Bob", allUsers.get(1).getName());
    }


    @Test
    @DisplayName("Создание пользователя с пустым именем должно выбрасывать исключение")
    void testCreateUserWithEmptyName() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser("", "test@example.com", 20)
        );
        assertEquals("Имя не может быть пустым", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Создание пользователя с null email должно выбрасывать исключение")
    void testCreateUserWithNullEmail() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser("Test", null, 20)
        );
        assertEquals("Email не может быть пустым", ex.getMessage());
    }

    @Test
    @DisplayName("Создание пользователя с возрастом > 150 должно выбрасывать исключение")
    void testCreateUserWithInvalidAge() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser("Test", "test@example.com", 200)
        );
        assertEquals("Возраст должен быть в диапазоне от 0 до 150 лет", ex.getMessage());
    }
}