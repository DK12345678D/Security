package com.app.demo.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.app.demo.model.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {
	Optional<Admin> findByPhoneNumber(String phoneNumber);

	Optional<Admin> findByEmail(String email);
	
	Optional<Admin> findByEmailIgnoreCase(String email);

	@Modifying
	@Transactional
	@Query("UPDATE Admin a SET a.inactiveUser = 1 WHERE a.id = :id")
	int softDeleteById(@Param("id") Long id);
}
