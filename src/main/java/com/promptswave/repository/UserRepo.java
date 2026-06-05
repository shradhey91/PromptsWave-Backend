package com.promptswave.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.promptswave.entity.User;
import com.promptswave.enums.Role;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRole(Role role);

    @Query("""
            SELECT u FROM User u
            WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))
            """)
    Page<User> searchUsers(@Param("query") String query, Pageable pageable);

    @Query("SELECT COUNT(pl) FROM PromptLike pl WHERE pl.user.id = :userId")
    long countLikesByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(sp) FROM UserSavedPrompt sp WHERE sp.user.id = :userId")
    long countSavedByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(ce) FROM PromptCopyEvent ce WHERE ce.user.id = :userId")
    long countCopiesByUserId(@Param("userId") Long userId);
}
