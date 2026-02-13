package net.jrodolfo.jobportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import net.jrodolfo.jobportal.model.Application;

public interface ApplicationRepository extends JpaRepository<Application, Long>{

}
