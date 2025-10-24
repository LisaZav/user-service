package ru.lisa.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.time.LocalDateTime;

@Relation(collectionRelation = "users", itemRelation = "user")
@Schema(description = "Модель пользователя с HATEOAS-ссылками")
public class UserModel extends RepresentationModel<UserModel> {

    @Schema(description = "Уникальный идентификатор", example = "1")
    private Long id;

    @Schema(description = "Имя пользователя", example = "Леонид")
    private String name;

    @Schema(description = "Email пользователя", example = "test@example.com")
    private String email;

    @Schema(description = "Возраст", example = "11")
    private Integer age;

    @Schema(description = "Дата создания записи")
    private LocalDateTime createdAt;

    public UserModel() {
    }

    public UserModel(Long id, String name, String email, Integer age, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.age = age;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Integer getAge() {
        return age;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // для десериализации @RequestBody
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}