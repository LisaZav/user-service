package ru.lisa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lisa.entity.User;
import ru.lisa.service.UserService;

import java.util.List;
import java.util.Scanner;

import static ru.lisa.entity.Command.CREATE;
import static ru.lisa.entity.Command.DELETE;
import static ru.lisa.entity.Command.EXIT;
import static ru.lisa.entity.Command.HELP;
import static ru.lisa.entity.Command.READ_ALL;
import static ru.lisa.entity.Command.UPDATE;


public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);
    private static Scanner scanner = new Scanner(System.in);
    private static UserService userService;

    public static void main(String[] args) {
        userService = new UserService();
        start();
    }

    private static void start() {
        log.info("User Server Started.");
        log.info(help());
        try {
            while (true) {
                switch (scanner.nextLine()) {
                    case CREATE -> createUser();
                    case READ_ALL -> listUsers();
                    case UPDATE -> updateUser();
                    case DELETE -> deleteUser();
                    case HELP -> log.info(help());
                    case EXIT -> System.exit(130);
                }
            }
        } catch (Exception e) {
            log.error("Error: ", e);
        }
    }

    private static String help() {
        return """
                
                
                           === CRUD Help ===
                Description                 Command
                
                Добавить пользователя:      %s
                Список всех пользователей:  %s
                Обновить пользователя:      %s
                Удалить пользователя:       %s
                Выход:                      %s
                
                Выберите опцию: 
                """.formatted(CREATE, READ_ALL, UPDATE, DELETE, EXIT);
    }

    private static void createUser() {
        log.info("Введите name: ");
        var name = scanner.nextLine();
        log.info("Введите email: ");
        var email = scanner.nextLine();
        log.info("Введите age: ");
        var age = scanner.nextInt();

        userService.createUser(name, email, age);
    }

    private static void listUsers() {
        List<User> users = userService.getAllUsers();
        if (users.isEmpty()) {
            log.info("Список пуст");
        } else {
            log.info("=== Список пользователей ===");
            users.forEach(System.out::println);
        }
    }

    private static void updateUser() {
        log.info("Введите ID пользователя для обновления: ");
        var id = Long.parseLong(scanner.nextLine());

        var user = userService.getUserById(id);
        if (user.isEmpty()) {
            log.info("Пользователь не найден!");
        } else {

            log.info("Новое имя (текущее: " + user.get().getName() + "): ");
            String name = scanner.nextLine();
            log.info("Новый email (текущий: " + user.get().getEmail() + "): ");
            String email = scanner.nextLine();

            userService.updateUser(id, name, email, user.get().getAge());
            log.info("Данные обновлены!");
        }
    }

    private static void deleteUser() {
        log.info("Введите ID пользователя для удаления: ");
        long id = Integer.parseInt(scanner.nextLine());

        if (userService.deleteUser(id)) {
            log.info("Пользователь удален!");
        } else {
            log.info("Пользователь не найден!");
        }
    }
}
