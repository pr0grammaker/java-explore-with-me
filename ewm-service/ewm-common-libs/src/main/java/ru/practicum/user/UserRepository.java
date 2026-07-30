package ru.practicum.user;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);
    boolean existsByName(String username);

    @Query("SELECT u FROM User u " +
            "WHERE (:ids IS NULL OR u.id IN :ids)")
    List<User> findAllByIds(@Param("ids") List<Long> ids, Pageable pageable);
}
