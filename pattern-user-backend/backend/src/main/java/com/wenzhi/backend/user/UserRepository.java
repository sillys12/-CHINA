package com.wenzhi.backend.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
  boolean existsByUsername(String username);

  Optional<UserEntity> findByUsername(String username);
}

