package ru.practicum.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.dto.user.NewUserDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mappers.UserMapper;
import ru.practicum.model.User;
import ru.practicum.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto addUser(NewUserDto newUserDto) {
        if (userRepository.existsByEmail(newUserDto.getEmail())) {
            throw new ConflictException("Пользователь с таким email уже существует");
        }
        User user = UserMapper.INSTANCE.getUser(newUserDto);
        return UserMapper.INSTANCE.getUserDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        userRepository.deleteById(userId);
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size,
                Sort.by(Sort.Direction.ASC, "id"));
        return userRepository.findAllByFilter(ids, pageable)
                .stream()
                .map(UserMapper.INSTANCE::getUserDto)
                .toList();
    }
}
