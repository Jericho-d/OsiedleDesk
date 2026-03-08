package com.administrativetool.repository;

import com.administrativetool.domain.model.User;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("SELECT * FROM users WHERE role_name = :role")
    Iterable<User> findByRole(@Param("role") String role);
}
