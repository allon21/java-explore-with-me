package ru.practicum.service;

import ru.practicum.dto.user.NewUserDto;
import ru.practicum.dto.user.UserDto;

import java.util.List;

public interface UserService {
    UserDto addUser(NewUserDto newUserDto);

    void deleteUser(Long userId);

    List<UserDto> getUsers(List<Long> ids, int from, int size);
}
