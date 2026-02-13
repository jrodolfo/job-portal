package net.jrodolfo.jobportal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import net.jrodolfo.jobportal.model.User;

public interface UserRepository extends JpaRepository<User, Long>{

	Optional<User> findByName(String username);
}
