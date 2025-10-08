package ru.lisa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.lisa.entity.User;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;


    @Test
    @DisplayName("Проверка создания пользователя")
    void testCreateUser() {
        // given
        String name = "Leon Fix";
        String email = "leon@rambler.com";
        Integer age = 24;

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        User savedUser = new User(name, email, age);
        savedUser.setId(1L); //  ID должен быть установлен
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // when
        Long actualId = userService.createUser(name, email, age);

        // then
        assertEquals(1L, actualId);
        verify(userRepository, times(1)).findByEmail(email);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Создание пользователя с существующим email должно выбрасывать исключение")
    void testCreateUserWithExistingEmail() {
        // given
        String name = "Leon Fix";
        String email = "leon@rambler.com";
        Integer age = 24;

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(name, email, age)
        );

        assertEquals("Пользователь с email 'leon@rambler.com' уже существует", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Получение пользователя по валидному ID")
    void testGetUserByIdWithValidId() {
        // given
        Long userId = 1L;
        User user = new User("Leon Fix", "leon@rambler.com", 24);
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        Optional<User> actual = userService.getUserById(userId);

        // then
        assertTrue(actual.isPresent());
        assertEquals(userId, actual.get().getId());
        assertEquals("Leon Fix", actual.get().getName());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("Получение пользователя по отрицательному ID должно выбрасывать исключение")
    void testGetUserByIdWithNegativeId() {
        // given
        Long userId = -1L;

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.getUserById(userId)
        );

        assertEquals("ID должен быть положительным числом", exception.getMessage());
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Проверка удаления по ID")
    void testDeleteUser() {
        // given
        long id = 1L;
        when(userRepository.existsById(id)).thenReturn(true); // пользователь существует

        // when
        boolean actual = userService.deleteUser(id);

        // then
        assertTrue(actual);
        verify(userRepository, times(1)).existsById(id);
        verify(userRepository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("Удаление несуществующего пользователя должно возвращать false")
    void testDeleteUserNotFound() {
        // given
        long id = 999L;
        when(userRepository.existsById(id)).thenReturn(false);

        // when
        boolean actual = userService.deleteUser(id);

        // then
        assertFalse(actual);
        verify(userRepository, times(1)).existsById(id);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Поиск всех пользователей")
    void testFindAll() {
        // given
        User user1 = new User("Alice", "alice@example.com", 30);
        User user2 = new User("Bob", "bob@example.com", 25);
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        // when
        List<User> allUsers = userService.getAllUsers();

        // then
        assertEquals(2, allUsers.size());
        assertEquals("Alice", allUsers.get(0).getName());
        assertEquals("Bob", allUsers.get(1).getName());
    }

    @Test
    @DisplayName("Проверка обновления пользователя (email не меняется)")
    void testUpdateUserWithoutEmailChange() {
        // given
        Long userId = 23L;
        String oldEmail = "old@example.com";
        User existingUser = new User("Old Name", oldEmail, 20);
        existingUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        // email не меняется → findByEmail не должен вызываться

        // when
        userService.updateUser(userId, "New Name", oldEmail, 30);

        // then
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).findByEmail(anyString()); // потому что email тот же
        verify(userRepository, times(1)).save(any(User.class));

        // Проверим, что поля обновились
        assertEquals("New Name", existingUser.getName());
        assertEquals(30, existingUser.getAge());
        assertEquals(oldEmail, existingUser.getEmail());
    }

    @Test
    @DisplayName("Обновление с конфликтом email должно выбрасывать исключение")
    void testUpdateUserWithConflictingEmail() {
        // given
        Long userId = 1L;
        User existingUser = new User("User1", "user1@example.com", 25);
        existingUser.setId(userId);

        // Другой пользователь уже использует новый email
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail("user2@example.com")).thenReturn(Optional.of(new User()));

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(userId, "New Name", "user2@example.com", 30)
        );

        assertEquals("Пользователь с email 'user2@example.com' уже существует", exception.getMessage());
        verify(userRepository, never()).save(any());
    }
}