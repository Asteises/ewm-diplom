package ru.praktikum.mainservice.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.praktikum.mainservice.exception.ConflictException;
import ru.praktikum.mainservice.exception.NotFoundException;
import ru.praktikum.mainservice.user.mapper.UserMapper;
import ru.praktikum.mainservice.user.model.User;
import ru.praktikum.mainservice.user.model.dto.UserDto;
import ru.praktikum.mainservice.user.repository.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;


    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {

        User user = UserMapper.userDtoToUser(userDto);

        checkUserNameAlreadyExist(user.getName());

        user = userStorage.save(user);

        log.info("Создан новый пользователь: {}", user);
        return UserMapper.userToUserDto(user);
    }

    @Override
    public List<UserDto> getUsersById(List<Long> ids, Integer from, Integer size) {

        List<User> users;

        if (ids.isEmpty()) {
            users = userStorage.findAll();
            log.info("Получаем всех пользователей: from={}, size={}", from, size);
        } else {
            users = userStorage.findUsersByIdIn(ids, PageRequest.of(from / size, size))
                    .stream().collect(Collectors.toList());
            log.info("Получаем всех пользователей: from={}, size={}, users={}", from, size, users.toString());
        }

        return users.stream()
                .map(UserMapper::userToUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUser(long userId) {

        User user = checkUserAvailableInDb(userId);

        log.info("Пользователь удален: userId={}", userId);
        userStorage.delete(user);
    }

    @Override
    public User checkUserAvailableInDb(long userId) {

        log.info("Проверяем наличие пользователя: userId={}", userId);
        return userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь не найден: userId=%s", userId)));
    }

    private void checkUserNameAlreadyExist(String userName) {

        log.info("Проверяем наличие имени пользователя: userName={}", userName);
        if (userStorage.findByName(userName).isPresent()) {
            throw new ConflictException(String.format("Пользователь с таким именем уже существует: userName=%s!", userName));
        }
    }
}
