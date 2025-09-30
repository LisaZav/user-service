package ru.lisa.dao;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.lisa.entity.User;
import ru.lisa.util.HibernateUtil;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@Testcontainers
// @TestInstance позволяет использовать нестатические методы с @BeforeAll
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserDaoImplTest {

    // Объявляем контейнер PostgreSQL, который будет под управлпением Testcontainers
    @Container
    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("my_db")
            .withUsername("developer")
            .withPassword("developer");

    private SessionFactory sessionFactory;
    private UserDaoImpl userDao;


    @BeforeAll
    void beforeAll() {
        // Динамически создаём SessionFactory с URL от контейнера
        Configuration configuration = new Configuration();
        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        configuration.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        configuration.setProperty("hibernate.connection.url", postgreSQLContainer.getJdbcUrl());
        configuration.setProperty("hibernate.connection.username", postgreSQLContainer.getUsername());
        configuration.setProperty("hibernate.connection.password", postgreSQLContainer.getPassword());
        configuration.setProperty("hibernate.hbm2ddl.auto", "create-drop"); // или update, если нужно
        configuration.setProperty("hibernate.show_sql", "true");
        configuration.setProperty("hibernate.format_sql", "true");

        configuration.addAnnotatedClass(User.class);

        sessionFactory = configuration.buildSessionFactory();

        HibernateUtil.setSessionFactory(sessionFactory);
    }

    @BeforeEach
    void setUp() {
        System.out.println("Инициализация UserDao перед тестом");
        userDao = new UserDaoImpl(sessionFactory);
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
        user.setCreatedAt(LocalDateTime.now());

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
