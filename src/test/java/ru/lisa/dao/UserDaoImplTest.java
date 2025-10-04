package ru.lisa.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.lisa.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        Configuration configuration = new Configuration();
        configuration.setProperty("hibernate.connection.url", postgreSQLContainer.getJdbcUrl());
        configuration.setProperty("hibernate.connection.username", postgreSQLContainer.getUsername());
        configuration.setProperty("hibernate.connection.password", postgreSQLContainer.getPassword());
        configuration.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        configuration.addAnnotatedClass(User.class);

        sessionFactory = configuration.buildSessionFactory();
    }

    @BeforeEach
    void setUp() {
        System.out.println("Инициализация UserDao перед тестом");
        userDao = new UserDaoImpl(sessionFactory);
    }

    @AfterEach
    void tearDown() {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        try {
            session.createMutationQuery("DELETE FROM User").executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw new RuntimeException("Ошибка при очистке базы данных", e);
        } finally {
            session.close();
        }
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

    @Test
    @DisplayName("Должен найти пользователя по ID")
    void testFindById() {

        // Given
        User user = new User();
        user.setEmail("sim@yandex.com");
        user.setName("Max Sim");
        Long userId = userDao.save(user);

        // When
        Optional<User> foundUser = userDao.findById(userId);

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals(userId, foundUser.get().getId(), "ID должен совпадать");
        assertEquals("Max Sim", foundUser.get().getName(), "Имя должно совпадать");
        assertEquals("sim@yandex.com", foundUser.get().getEmail());
    }

    @Test
    @DisplayName("Должен найти пользователя по email")
    void findUserByEmail() {
        // Given
        User user = new User();
        user.setEmail("bm@ya.com");
        user.setName("Bob Morly");
        userDao.save(user);

        //  When
        Optional<User> foundUser = userDao.findByEmail("bm@ya.com");

        // Then
        assertTrue(foundUser.isPresent(), "Пользователь с указанным email должен быть найден");
        assertEquals("Bob Morly", foundUser.get().getName(), "Имя должно совпадать");
        assertEquals("bm@ya.com", foundUser.get().getEmail(), "Email должен совпадать");
    }

    @Test
    @DisplayName("Должен вернуть пустой результат при поиске по несуществующему ID")
    void returnEmptyWhenUserNotFoundById() { // Не создаем пользователей - база пуста
        // Given

        //  When
        Optional<User> foundUser = userDao.findById(999L);

        // Then
        assertFalse(foundUser.isPresent());
    }

    @Test
    @DisplayName("Должен вернуть всех пользователей")
    void findAllUsers() {
        // Given
        User user1 = new User();
        user1.setEmail("bm2@ya.com");
        user1.setName("Bob Morly");
        userDao.save(user1);

        User user2 = new User();
        user2.setEmail("sorvarayavish@ya.com");
        user2.setName("Vishenka Sorvanaya");
        userDao.save(user2);
        // When
        List<User> users = userDao.findAll();

        // Then
        assertNotNull(users, "Список пользователей не должен быть null");
        assertEquals(2, users.size(), "Должны вернуться ровно 2 пользователя");
    }

    @Test
    @DisplayName("Должен обновить данные пользователя")
    void testUpdateUser() {
        // Given
        User user = new User();
        user.setEmail("update@rambler.com");
        user.setName("Original");
        Long userId = userDao.save(user);

        var byId = userDao.findById(userId).get();
        byId.setName("NewName");

        // When
        userDao.update(byId);
        // Then
        User updatedUser = userDao.findById(userId).get();
        assertEquals("NewName", updatedUser.getName());
    }

    @Test
    @DisplayName("Удаление пользователя по ID")
    void testDeleteUserById() {
        // Given
        User user = new User();
        user.setEmail("delete@example.com");
        user.setName("V");

        Long userId = userDao.save(user);

        // When
        boolean deleteResult = userDao.delete(userId);

        // Then
        assertTrue(deleteResult);
        assertTrue(userDao.findById(userId).isEmpty(), "Пользователь должен быть удален из базы");
    }
}

