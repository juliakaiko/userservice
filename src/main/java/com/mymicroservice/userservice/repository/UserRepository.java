package com.mymicroservice.userservice.repository;

import com.mymicroservice.userservice.model.Role;
import com.mymicroservice.userservice.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import java.time.LocalDate;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by email address using "named method".
     *
     * @param email the email address to search for (case-insensitively)
     * @return an {@link Optional} containing the user if found, empty otherwise
     * @throws IllegalArgumentException if email is null
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Finds all users with specified IDs using "named method".
     *
     * @param ids set of user IDs to search for
     * @return a list of users matching the provided IDs (may be empty)
     * @throws IllegalArgumentException if ids set is null
     */
    List<User> findByUserIdIn(Set<Long> ids);

    /**
     * Finds all users with the specified role using JPQL query.
     *
     * @param role the user role to filter by
     * @return a list of users with the given role (may be empty)
     * @throws IllegalArgumentException if role is null
     */
    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findUsersByRole(@Param("role") Role role);

    /**
     * Finds all users born after the specified date using native SQL.
     *
     * @param date the cutoff date (must not be null)
     * @return a list of users born after the given date
     * @throws IllegalArgumentException if date is null
     */
    @Query(value = "SELECT * FROM users WHERE birth_date > :date", nativeQuery = true)
    List<User> findUsersBornAfter(@Param("date") LocalDate date);

    /**
     * Retrieves all users with pagination support using native SQL.
     * <p>
     * Results are ordered by user ID in ascending order.
     *
     * @param pageable pagination configuration (page number, size, etc.)
     * @return a {@link Page} of users with pagination information
     * @throws IllegalArgumentException if pageable is null
     */
    @Query(value = "select * from users order by users.id asc", nativeQuery = true)
    Page<User> findAllUsersNative(Pageable pageable);

}

