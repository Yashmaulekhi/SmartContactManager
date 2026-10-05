package com.project.manage.Dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import com.project.manage.entities.User;
@Service
public interface UserRepository extends JpaRepository<User,Integer>{
	@Query(value = "SELECT * FROM user WHERE email = :email ",  nativeQuery = true)
	User getUserByUserEmail(@Param("email") String email);
}
