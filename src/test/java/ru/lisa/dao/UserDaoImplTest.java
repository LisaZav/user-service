package ru.lisa.dao;

import org.hibernate.Session;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.lisa.entity.User;
import ru.lisa.util.HibernateUtil;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@Testcontainers
// @TestInstance позволяет использовать нестатические методы с @BeforeAll
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserDaoImplTest {

    // Объявляем контейнер PostgreSQL, который будет под управлпением Testcontainers
    @Container
    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test");

    private UserDaoImpl userDao;


    @BeforeAll
        // метод перед всеми тестами
    void beforeAll() {
        System.out.println("Запускаеем контейнер PostgreSQL: " + postgreSQLContainer.getJdbcUrl());

        // переопределение настройки Hibernate для подключения к контейнеру
        System.setProperty("hibernate.connection.url", postgreSQLContainer.getJdbcUrl());
        System.setProperty("hibernate.connection.username", postgreSQLContainer.getUsername());
        System.setProperty("hibernate.connection.password", postgreSQLContainer.getPassword());

        // Пересоздание SFactory с новыми настройками
        HibernateUtil.resetSessionFactory();
    }

    @BeforeEach
    void setUp() {
        System.out.println("Инициализация UserDao перед тестом");
        userDao = new UserDaoImpl();
    }

    @AfterEach
    void tearDown() {
        System.out.println("Очистка после теста"); //отчистка БД если она нужна
    }

    @Test
    @DisplayName("Должен сохранить пользователя и назначить ID")
    void shouldSaveUser() {
        // Given
        User user = new User();
        user.setName("Maria Sam");
        user.setEmail("ma@rambler.com");

        // When
        Long userId = userDao.save(user);

        // Then
        assertNotNull(userId, "ID пользователя не должен быть null");
        assertTrue(userId > 0, "ID должен быть положительным числом");

        Optional<User> savedUser = userDao.findById(userId);
        assertTrue(savedUser.isPresent(), "Пользователь должен существовать в базе");
        assertEquals("Maria Sam", savedUser.get().getName());
        assertEquals("ma@rambler.com", savedUser.get().getEmail());
    }
}
