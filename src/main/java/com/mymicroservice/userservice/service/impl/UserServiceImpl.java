package com.mymicroservice.userservice.service.impl;

import com.mymicroservice.userservice.dto.UserDto;
import com.mymicroservice.userservice.exception.NotFoundException;
import com.mymicroservice.userservice.mapper.UserMapper;
import com.mymicroservice.userservice.model.Role;
import com.mymicroservice.userservice.model.User;
import com.mymicroservice.userservice.repository.UserRepository;
import com.mymicroservice.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "usersCache")
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    /**
     * Создает нового пользователя на основе переданного DTO.
     *
     * @param userDto DTO с данными пользователя.
     * @return DTO созданного пользователя.
     */
    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        User user = UserMapper.INSTANSE.toEntity(userDto);
        log.info("createUser(): {}",user);
        user = userRepository.save(user);
        return UserMapper.INSTANSE.toDto(user);
    }

    /**
     * Возвращает пользователя по его ID. Результат кэшируется в "userCache" с ключом по userId.
     * При последующих запросах с тем же ID будет возвращаться значение из кэша, минуя базу данных.
     *
     * @param userId ID пользователя для поиска
     * @return DTO найденного пользователя
     * @throws NotFoundException если пользователь с указанным ID не найден в базе данных
     * @see org.springframework.cache.annotation.Cacheable
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userCache", key = "#userId")
    public UserDto getUsersById(Long userId) {
        Optional<User> user = Optional.ofNullable(userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User wasn't found with id " + userId)));
        log.info("getUsersById(): {}",userId);
        return UserMapper.INSTANSE.toDto(user.get());
    }

    /**
     * Обновляет данные пользователя (name, surname, birthDate, email, пароль и роль)
     * и обновляет соответствующую запись в кэше "userCache" с ключом по userId.
     *
     * @param userId ID пользователя для обновления
     * @param userDetails DTO с обновлёнными данными пользователя
     * @return DTO обновлённого пользователя
     * @throws NotFoundException если пользователь с указанным ID не найден в базе данных
     * @see org.springframework.cache.annotation.CachePut
     */
    @Override
    @Transactional
    @CachePut(value = "userCache", key = "#userId") // update in cache
    public UserDto updateUser(Long userId, UserDto userDetails) {
        Optional<User> userFromDb = Optional.ofNullable(userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User wasn't found with id " + userId)));
        User user = userFromDb.get();
        user.setName(userDetails.getName());
        user.setSurname(userDetails.getSurname());
        user.setBirthDate(userDetails.getBirthDate());
        user.setEmail(userDetails.getEmail());
        user.setPassword(userDetails.getPassword());
        user.setRole(userDetails.getRole());
        log.info("updateUser(): {}",user);
        userRepository.save(user);
        return UserMapper.INSTANSE.toDto(user);
    }

    /**
     * Удаляет пользователя по его ID и удаляет соответствующую запись из кэша "userCache".
     *
     * @param userId ID пользователя для удаления
     * @return DTO удалённого пользователя
     * @throws NotFoundException если пользователь с указанным ID не найден в базе данных
     * @see org.springframework.cache.annotation.CacheEvict
     */
    @Override
    @Transactional
    @CacheEvict(value = "userCache", key = "#userId") // delete from cache
    public UserDto deleteUser(Long userId) {
        Optional<User> user = Optional.ofNullable(userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User wasn't found with id " + userId)));
        userRepository.deleteById(userId);
        log.info("deleteUser(): {}",user);
        return UserMapper.INSTANSE.toDto(user.get());
    }

    /**
     * Возвращает пользователя по его email.
     *
     * @param email email пользователя.
     * @return DTO найденного пользователя.
     * @throws NotFoundException если пользователь с указанным email не найден.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDto getUsersByEmail(String email) {
        Optional<User> user = Optional.ofNullable(userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("User wasn't found with email " + email)));
        log.info("getUsersByEmail(): {}",user);
        return UserMapper.INSTANSE.toDto(user.get());
    }

    /**
     *Возвращает список пользователей по заданному набору идентификаторов.
     *
     * @param ids Набор идентификаторов пользователей для поиска
     * @return Список DTO пользователей с указанными идентификаторами
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsersIdIn(Set<Long> ids) {
        List <User> userList = userRepository.findByUserIdIn(ids);
        log.info("getUsersIdIn()");
        return userList.stream().map(UserMapper.INSTANSE::toDto).toList();
    }

    /**
     * Возвращает список пользователей по ролям.
     *
     * @param role role пользователя.
     * @return Список DTO пользователей с указанной ролью.
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsersByRole(Role role) {
        List <User> userList = userRepository.findUsersByRole(role);
        log.info("getUsersByRole(): {}",role);
        return userList.stream().map(UserMapper.INSTANSE::toDto).toList();
    }

    /**
     * Возвращает список пользователей, рожденных после указанной даты.
     *
     * @param date Дата рождения в формате строки
     * @return Список DTO пользователей, чья дата рождения строго позже указанной даты
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsersBornAfter(LocalDate date) {
        List <User> userList = userRepository.findUsersBornAfter(date);
        log.info("getUsersBornAfter(): {}",date);
        return userList.stream().map(UserMapper.INSTANSE::toDto).toList();
    }

    /**
     * Возвращает список всех пользователей.
     *
     * @return Список DTO всех пользователей.
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        List <User> userList = userRepository.findAll();
        log.info("getAllUsers()");
        return userList.stream().map(UserMapper.INSTANSE::toDto).toList();
    }

    /**
     * Возвращает страницу с пользователями, используя нативную пагинацию и сортировку по ID.
     *
     * @param page Номер страницы (начиная с 0).
     * @param size Количество пользователей на странице.
     * @return Страница с DTO пользователей.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsersNativeWithPagination(Integer page, Integer size) {
        var pageable  = PageRequest.of(page,size, Sort.by("id"));
        Page<User> userList = userRepository.findAllUsersNative(pageable);
        log.info("findAllUsersNativeWithPagination()");
        return userList.map(UserMapper.INSTANSE::toDto);
    }
}
