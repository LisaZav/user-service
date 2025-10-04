package ru.lisa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.lisa.dao.UserDao;
import ru.lisa.entity.User;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Проверка создания пользователя")
    void testCreateUser() {
        //given
        String name = "Leon Fix";
        String email = "leon@rambler.com";
        Integer age = 24;
        when(userDao.save(any())).thenReturn(1L);
        when(userDao.findByEmail(email)).thenReturn(Optional.empty());
        //when
        long actual = userService.createUser(name, email, age);
        //then
        verify(userDao, times(1)).save(any());
        verify(userDao, times(1)).findByEmail(any());
        assertEquals(1L, actual);
    }

    @Test
    @DisplayName("Создание пользователя с существующим email должно выбрасывать исключение")
    void testCreateUserWithExistingEmail() {
        //given
        String name = "Leon Fix";
        String email = "leon@rambler.com";
        Integer age = 24;

        when(userDao.findByEmail(any())).thenReturn(Optional.of(new User()));
        //when & then
        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(name, email, age));

        verify(userDao, never()).save(any());
    }

    @Test
    @DisplayName("Получение пользователя по валидному ID")
    void testGetUserByIdWithValidId() {
        //given
        Long userId = 1L;
        User user = new User("Leon Fix", "leon@rambler.com", 24);
        when(userDao.findById(userId)).thenReturn(Optional.of(user));

        //when
        Optional<User> actual = userService.getUserById(userId);

        //then
        assertTrue(actual.isPresent());
        assertEquals(user, actual.get());
        verify(userDao, times(1)).findById(userId);
    }

    @Test
    @DisplayName("Получение пользователя по отрицательному ID должно выбрасывать исключение")
    void testGetUserByIdWithNegativeId() {
        //given
        Long userId = -1L;

        //when & then
        assertThrows(IllegalArgumentException.class,
                () -> userService.getUserById(userId));

        verify(userDao, never()).findById(anyLong());

    }

    @Test
    @DisplayName("Проверка удаления по ID")
    void testDeleteUser() {
        //given
        long id = 1L;
        when(userDao.delete(id)).thenReturn(true);
        //when
        boolean actual = userService.deleteUser(id);
        //then
        verify(userDao, times(1)).delete(any());
        assertTrue(actual);
    }

    @Test
    @DisplayName("Поиск всех пользователей")
    void testFindAll() {
        //  given
        when(userDao.findAll()).thenReturn(List.of(new User()));
        // when
        var allUsers = userService.getAllUsers();
        // then
        assertEquals(allUsers.size(), 1);
    }

    @Test
    @DisplayName("Проверка обновления пользователя")
    void testUpdateUser() {
        //given
        String name = "Leon Fix";
        String email = "leon@rambler.com";
        Integer age = 24;

        var user = new User(name, email, age);
        user.setId(23L);
        when(userDao.findById(any())).thenReturn(Optional.of(user));
        // when
        userService.updateUser(23L, name, email, age);
        //then
        verify(userDao).update(any());
        verify(userDao, never()).findByEmail(any());
    }
}
