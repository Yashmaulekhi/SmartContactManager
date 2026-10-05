package com.project.manage.Dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.manage.entities.Contact;
import com.project.manage.entities.User;

public interface ContactRepository extends JpaRepository<Contact, Integer> {
	@Query(
		    value = "SELECT * FROM contact c WHERE c.user_id = :userId",
		    nativeQuery = true
		)
		public Page<Contact> findContactsByUser(@Param("userId") int userId,Pageable pageable);
	public List<Contact>findByNameContainingAndUser(String name,User user);
}
