package ru.lisa.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.lisa.dto.UserDto;
import ru.lisa.entity.User;
import ru.lisa.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto request) {
        Long userId = userService.createUser(request.getName(), request.getEmail(), request.getAge());
        var user = userService.getUserById(userId).orElseThrow();
        return ResponseEntity.status(HttpStatus.CREATED).body(UserDto.fromEntity(user));
    }
}



