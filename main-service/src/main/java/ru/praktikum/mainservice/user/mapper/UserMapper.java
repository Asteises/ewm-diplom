package ru.praktikum.mainservice.user.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.praktikum.mainservice.user.model.User;
import ru.praktikum.mainservice.user.model.dto.UserDto;
import ru.praktikum.mainservice.user.model.dto.UserShortDto;

@Slf4j
@Service
public class UserMapper {

    public static UserDto userToUserDto(User user) {

        log.info("Маппим User в UserDto");
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getName()
        );
    }

    public static User userDtoToUser(UserDto userDto) {

        User user = new User();
        user.setId(userDto.getId());
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());

        log.info("Маппим UserDto в User");
        return user;
    }

    public static UserShortDto userToUserShortDto(User user) {

        log.info("Маппим User в UserShortDto");
        return new UserShortDto(user.getId(), user.getName());
    }
}
