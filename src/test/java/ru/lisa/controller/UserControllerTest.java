package ru.lisa.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.lisa.dto.UserDto;
import ru.lisa.entity.User;
import ru.lisa.service.UserService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; //преобразовывает объект в JSON-строку

    @MockBean
    private UserService userService;

    @BeforeEach
    void setUp() {
        // Регистрация модуля для поддержки LocalDateTime в JSON
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("POST /api/users — успешно создаёт пользователя")
    void CreateUser() throws Exception {
        UserDto requestDto = new UserDto();
        requestDto.setName("Alice");
        requestDto.setEmail("alice@rambler.com");
        requestDto.setAge(36);

        User savedUser = new User("Alice", "alice@rambler.com", 36);
        savedUser.setId(1L);

        when(userService.createUser(anyString(), anyString(), anyInt())).thenReturn(1L);
        when(userService.getUserById(1L)).thenReturn(Optional.of(savedUser));

        mockMvc.perform(post("/api/users")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice@rambler.com"))
                .andExpect(jsonPath("$.age").value(36))
                .andExpect(jsonPath("$.createdAt").exists()); // просто проверяем, что поле есть

        verify(userService).createUser("Alice", "alice@rambler.com", 36);
    }

    @Test
    @DisplayName("GET /api/users/{id} — возвращает пользователя по ID")
    void dGetUserById() throws Exception {
        User user = new User("Bob", "bob@ya.com", 25);
        user.setId(2L); // id устанавливается отдельно, createdAt — уже в конструкторе

        when(userService.getUserById(2L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Bob"))
                .andExpect(jsonPath("$.email").value("bob@ya.com"))
                .andExpect(jsonPath("$.age").value(25))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @DisplayName("PUT /api/users — успешно обновляет пользователя")
    void UpdateUser() throws Exception {
        UserDto updateDto = new UserDto();
        updateDto.setId(1L);
        updateDto.setName("Alice Updated");
        updateDto.setEmail("alice@example.com");
        updateDto.setAge(31);

        mockMvc.perform(put("/api/users")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                        .andExpect(status().isOk());

        verify(userService).updateUser(1L, "Alice Updated", "alice@example.com", 31);
    }

    @Test
    @DisplayName("POST /api/users — возвращает ошибку при попытке создать пользователя с уже существующим email")
    void ReturnConflictWithExistingEmail() throws Exception {
        UserDto requestDto = new UserDto();
        requestDto.setName("Alice");
        requestDto.setEmail("alice@rambler.com");
        requestDto.setAge(36);

        when(userService.createUser(anyString(), anyString(), anyInt()))
                .thenThrow(new IllegalArgumentException("Пользователь с email 'alice@rambler.com' уже существует"));

        mockMvc.perform(post("/api/users")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/users/{id} — delete пользователя по ID")
    void deleteUserById() throws Exception {
        User user = new User("Bob", "bob@ya.com", 25);
        user.setId(2L); // id устанавливается отдельно, createdAt — уже в конструкторе

        when(userService.deleteUser(2L)).thenReturn(true);

        mockMvc.perform(delete("/api/users/2"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("UPDATE /api/users — update пользователя")
    void updateUser() throws Exception {
        User user = new User("Bob", "bob@ya.com", 25);

        mockMvc.perform(put("/api/users")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/users — get all пользователей")
    void getAllUsers() throws Exception {

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());
    }
}
