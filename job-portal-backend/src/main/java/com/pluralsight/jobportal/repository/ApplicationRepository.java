package com.pluralsight.jobportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pluralsight.jobportal.model.Application;

public interface ApplicationRepository extends JpaRepository<Application, Long>{

}
