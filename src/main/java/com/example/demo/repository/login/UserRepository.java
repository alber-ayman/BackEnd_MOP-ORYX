package com.example.demo.repository.login;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.models.User;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);

  Boolean existsByUsername(String username);

  Boolean existsByEmail(String email);

    public User findAllById(Long id);

  @Query(value = "select username from users where manufacturing_manager = 1", nativeQuery = true)
  List<String> getFactoryManagerName();
}
