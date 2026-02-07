package com.pluralsight.jobportal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pluralsight.jobportal.model.User;

public interface UserRepository extends JpaRepository<User, Long>{

	Optional<User> findByName(String username);
}
