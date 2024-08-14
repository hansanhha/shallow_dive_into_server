package com.docker.user;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/user")
    public Map<String, Object> register(@RequestBody UserCreateRequest request) {
        User register = userService.register(request.id(), request.password(), request.nickname());
        return Map.of("message", "success", "createdUser", register);
    }

    @GetMapping("/users")
    public Map<String, Object> getUsers() {
        return Map.of("message", "success", "users", userService.getUsers());
    }

    @GetMapping("/users/{id}")
    public Map<String, Object> getUser(@PathVariable Long id) {
        User user = userService.getUser(id);
        return Map.of("message", "success", "user", user);
    }

}
