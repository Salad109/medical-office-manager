package io.salad109.medicalofficemanager.users.internal;

import io.salad109.medicalofficemanager.users.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Page<User> findByRole(Role role, Pageable pageable);

    boolean existsByUsername(String username);

    boolean existsByPhoneNumber(String phoneNumber);

    @Query(value = "SELECT * FROM users WHERE MATCH(first_name, last_name) AGAINST (:query IN BOOLEAN MODE)",
            nativeQuery = true)
    Page<User> searchByName(@Param("query") String query, Pageable pageable);
}
