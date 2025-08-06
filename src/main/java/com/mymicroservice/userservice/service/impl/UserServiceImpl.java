package com.mymicroservice.userservice.service.impl;

import com.mymicroservice.userservice.dto.UserDto;
import com.mymicroservice.userservice.exception.UserNotFoundException;
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
@CacheConfig(cacheNames = "userCache")
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    /**
     * Creates a new User based on the provided DTO.
     *
     * @param userDto DTO containing user data.
     * @return DTO of the created user.
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
     * Returns the User by its ID. The result is cached in "userCache" with the userId as the key.
     * Subsequent requests with the same ID will return the value from the cache, bypassing the database.
     *
     * @param userId ID of the user to find
     * @return DTO of the found user
     * @throws UserNotFoundException if the User with the specified ID is not found in the database
     * @see org.springframework.cache.annotation.Cacheable
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userCache", key = "#userId")
    public UserDto getUserById(Long userId) {
        Optional<User> user = Optional.ofNullable(userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User wasn't found with id " + userId)));
        log.info("getUsersById(): {}",userId);
        return UserMapper.INSTANSE.toDto(user.get());
    }

    /**
     * Updates User data (name, surname, birthDate, email, password and role)
     * and updates the corresponding data in the "userCache" with the userId as the key.
     *
     * @param userId ID of the User to update
     * @param userDetails DTO containing updated User data
     * @return DTO of the updated User
     * @throws UserNotFoundException if the User with the specified ID is not found in the database
     * @see org.springframework.cache.annotation.CachePut
     */
    @Override
    @Transactional
    @CachePut(value = "userCache", key = "#userId") // update in cache
    public UserDto updateUser(Long userId, UserDto userDetails) {
        Optional<User> userFromDb = Optional.ofNullable(userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User wasn't found with id " + userId)));
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
     * Deletes the User by their ID and removes the corresponding data from the "userCache".
     *
     * @param userId ID of the User to delete
     * @return DTO of the deleted User
     * @throws UserNotFoundException if the User with the specified ID is not found in the database
     * @see org.springframework.cache.annotation.CacheEvict
     */
    @Override
    @Transactional
    @CacheEvict(value = "userCache", key = "#userId") // delete from cache
    public UserDto deleteUser(Long userId) {
        Optional<User> user = Optional.ofNullable(userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User wasn't found with id " + userId)));
        userRepository.deleteById(userId);
        log.info("deleteUser(): {}",user);
        return UserMapper.INSTANSE.toDto(user.get());
    }

    /**
     * Returns the User by their email.
     *
     * @param email email of the user to find
     * @return DTO of the found User
     * @throws UserNotFoundException if the User with the specified email is not found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDto getUsersByEmail(String email) {
        Optional<User> user = Optional.ofNullable(userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("User wasn't found with email " + email)));
        log.info("getUsersByEmail(): {}",user);
        return UserMapper.INSTANSE.toDto(user.get());
    }

    /**
     * Returns a list of Users by the specified set of IDs.
     *
     * @param ids Set of User IDs to search for
     * @return List of UserDtos with the specified IDs
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsersIdIn(Set<Long> ids) {
        List <User> userList = userRepository.findByUserIdIn(ids);
        log.info("getUsersIdIn()");
        return userList.stream().map(UserMapper.INSTANSE::toDto).toList();
    }

    /**
     * Returns a list of Users by role.
     *
     * @param role User role to filter by
     * @return List of UserDtos with the specified role
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsersByRole(Role role) {
        List <User> userList = userRepository.findUsersByRole(role);
        log.info("getUsersByRole(): {}",role);
        return userList.stream().map(UserMapper.INSTANSE::toDto).toList();
    }

    /**
     * Returns a list of Users born after the specified date.
     *
     * @param date birth date in LocalDate format
     * @return List of UserDtos with birth dates strictly after the specified date
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsersBornAfter(LocalDate date) {
        List <User> userList = userRepository.findUsersBornAfter(date);
        log.info("getUsersBornAfter(): {}",date);
        return userList.stream().map(UserMapper.INSTANSE::toDto).toList();
    }

    /**
     * Returns a list of all Users.
     *
     * @return List of all UserDtos
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        List <User> userList = userRepository.findAll();
        log.info("getAllUsers()");
        return userList.stream().map(UserMapper.INSTANSE::toDto).toList();
    }

    /**
     * Returns a page of Users using native pagination sorted by ID.
     *
     * @param page Page number (0-based index)
     * @param size Number of Users per page
     * @return Page of user DTOs
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
