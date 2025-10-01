package ru.lisa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.lisa.dao.UserDao;
import ru.lisa.dao.UserDaoImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) //1
class UserServiceTest {

    @Mock //2
    private UserDao userDao;

    @InjectMocks //3
    private UserService userService;

    @Test //4
    @DisplayName("Проверка удаления")
    void testDeleteUser() {
        //given
        long id = 1L;
        when(userDao.delete(id)).thenReturn(true);
        //when
        boolean actual = userService.deleteUser(id);

        //then
        verify(userDao, times(1)).delete(any());
        assertTrue(actual);
        System.out.println("👍🏿👍🏿👍🏿👍🏿👍🏿👍🏿👍🏿👍🏿👍🏿👍🏿👍🏿👍🏿👍🏿👍🏿👍🏿👍🏿👍🏿👍🏿👍🏿");
    }
}