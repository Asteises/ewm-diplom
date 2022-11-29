package ru.praktikum.mainservice.user.service;

import org.springframework.stereotype.Service;
import ru.praktikum.mainservice.user.model.User;
import ru.praktikum.mainservice.user.model.dto.UserDto;

import java.util.List;

@Service
public interface UserService {

    UserDto createUser(UserDto userDto);

    List<UserDto> getUsersById(List<Long> ids, Integer from, Integer size);

    void deleteUser(long userId);

    User checkUserAvailableInDb(long userId);
}
