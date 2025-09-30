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
        System.out.println("User Server Started ");
        System.out.println(help());
        while (true) {
            try {
                switch (scanner.nextLine()) {
                    case CREATE -> createUser();
                    case READ_ALL -> listUsers();
                    case UPDATE -> updateUser();
                    case DELETE -> deleteUser();
                    case HELP -> System.out.println(help());
                    case EXIT -> System.exit(130);
                    default -> System.out.println("Ошибка, выберите команду из предложенного меню " + help());
                }


            } catch (RuntimeException e) {
                log.error("Произошла ошибка: ", e);
                System.out.println("Ошибка, пожалуйста повторите попытку ");
            }
        }
    }

    private static String help() {
        return """
                
                
      
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
        System.out.println("Введите name: ");
        var name = scanner.nextLine();
        System.out.println("Введите email: ");
        var email = scanner.nextLine();
        System.out.println("Введите age: ");
        var age = scanner.nextInt();

        userService.createUser(name, email, age);
    }

    private static void listUsers() {
        List<User> users = userService.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("Список пуст");
        } else {
            System.out.println("=== Список пользователей ===");
            users.forEach(System.out::println);
        }
    }

    private static void updateUser() {
        System.out.println("Введите ID пользователя для обновления: ");
        long id = Long.parseLong(scanner.nextLine());

        var user = userService.getUserById(id);
        if (user.isEmpty()) {
            System.out.println("Пользователь не найден!");
            return;
        }

        User currentUser = user.get();

        System.out.println("Какие данные вы хотите изменить?");
        System.out.println("1 - Только имя");
        System.out.println("2 - Только email");
        System.out.println("3 - Только возраст");
        System.out.println("4 - Несколько полей");
        System.out.println("0 - Отмена");

        String choice = scanner.nextLine();

        String newName = currentUser.getName();
        String newEmail = currentUser.getEmail();
        int newAge = currentUser.getAge();

        switch (choice) {
            case "1":
                System.out.print("Введите новое имя: ");
                newName = scanner.nextLine();
                break;

            case "2":
                System.out.print("Введите новый email: ");
                newEmail = scanner.nextLine();
                break;

            case "3":
                System.out.print("Введите новый возраст: ");
                newAge = Integer.parseInt(scanner.nextLine());
                break;

            case "4":
                // Можно оставить ваш исходный код для множественного обновления
                System.out.print("Введите новое имя (текущее: " + currentUser.getName() + "): ");
                String nameInput = scanner.nextLine();
                if (!nameInput.isEmpty()) newName = nameInput;

                System.out.print("Введите новый email (текущее: " + currentUser.getEmail() + "): ");
                String emailInput = scanner.nextLine();
                if (!emailInput.isEmpty()) newEmail = emailInput;

                System.out.print("Введите новый возраст (текущее: " + currentUser.getAge() + "): ");
                String ageInput = scanner.nextLine();
                if (!ageInput.isEmpty()) newAge = Integer.parseInt(ageInput);
                break;

            case "0":
                System.out.println("Отмена операции ");
                return;

            default:
                System.out.println("Неверный выбор!");
                return;
        }

        userService.updateUser(id, newName, newEmail, newAge);
        System.out.println("Данные обновлены!");
    }

    private static void deleteUser() {
        System.out.println("Введите ID пользователя для удаления: ");
        long id = Integer.parseInt(scanner.nextLine());


        if (userService.deleteUser(id)) {
            System.out.println("Пользователь удален!");
        } else {
            System.out.println("Пользователь не найден!");
        }
    }
}
