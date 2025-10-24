package ru.lisa.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.lisa.dto.UserModel;
import ru.lisa.entity.User;
import ru.lisa.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Операции управления пользователями")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "Создать нового пользователя")
    @ApiResponse(responseCode = "200", description = "Пользователь успешно создан",
            content = @Content(schema = @Schema(implementation = UserModel.class)))
    public ResponseEntity<EntityModel<UserModel>> createUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные нового пользователя", required = true
            )
            @RequestBody UserModel request) {

        Long userId = userService.createUser(request.getName(), request.getEmail(), request.getAge());
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Созданный пользователь не найден"));
        UserModel userModel = toUserModel(user);

        EntityModel<UserModel> model = EntityModel.of(userModel,
                linkTo(methodOn(UserController.class).getUserById(userId)).withSelfRel(),
                linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users")
        );
        return ResponseEntity.ok(model);
    }

    @PutMapping
    @Operation(summary = "Обновить существующего пользователя")
    @ApiResponse(responseCode = "200", description = "Пользователь успешно обновлён")
    public ResponseEntity<EntityModel<UserModel>> updateUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Обновлённые данные пользователя", required = true
            )
            @RequestBody UserModel dto) {

        userService.updateUser(dto.getId(), dto.getName(), dto.getEmail(), dto.getAge());
        User updatedUser = userService.getUserById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Обновлённый пользователь не найден"));
        UserModel userModel = toUserModel(updatedUser);

        EntityModel<UserModel> model = EntityModel.of(userModel,
                linkTo(methodOn(UserController.class).getUserById(dto.getId())).withSelfRel(),
                linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users")
        );
        return ResponseEntity.ok(model);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить пользователя по ID")
    @ApiResponse(responseCode = "200", description = "Пользователь найден",
            content = @Content(schema = @Schema(implementation = UserModel.class)))
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    public ResponseEntity<EntityModel<UserModel>> getUserById(
            @Parameter(description = "ID пользователя", required = true) @PathVariable Long id) {

        User user = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь с ID " + id + " не найден"));
        UserModel userModel = toUserModel(user);

        EntityModel<UserModel> model = EntityModel.of(userModel,
                linkTo(methodOn(UserController.class).getUserById(id)).withSelfRel(),
                linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users")
        );
        return ResponseEntity.ok(model);
    }

    @GetMapping
    @Operation(summary = "Получить всех пользователей")
    @ApiResponse(responseCode = "200", description = "Список пользователей",
            content = @Content(array = @io.swagger.v3.oas.annotations.media.ArraySchema(
                    schema = @Schema(implementation = UserModel.class))))
    public ResponseEntity<CollectionModel<EntityModel<UserModel>>> getAllUsers() {

        List<User> users = userService.getAllUsers();
        List<EntityModel<UserModel>> models = users.stream()
                .map(this::toUserModel)
                .map(userModel -> EntityModel.of(userModel,
                        linkTo(methodOn(UserController.class).getUserById(userModel.getId())).withSelfRel()))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<UserModel>> collectionModel = CollectionModel.of(models,
                linkTo(methodOn(UserController.class).getAllUsers()).withSelfRel()
        );
        return ResponseEntity.ok(collectionModel);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить пользователя по ID")
    @ApiResponse(responseCode = "204", description = "Пользователь успешно удалён")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID пользователя", required = true) @PathVariable Long id) {

        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            throw new RuntimeException("Пользователь с ID " + id + " не найден");
        }
    }

    private UserModel toUserModel(User user) {
        return new UserModel(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAge(),
                user.getCreatedAt()
        );
    }
}