package com.promptswave.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.promptswave.entity.ContactMessage;

@Repository
public interface ContactMessageRepo extends JpaRepository<ContactMessage, Long> {

}