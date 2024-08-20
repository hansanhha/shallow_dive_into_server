package com.docker.user;

public record UserCreateRequest(String id, String password, String nickname) {
}
