package com.tranthai.repository;

import com.tranthai.model.SupportChat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupportChatRepository extends JpaRepository<SupportChat, Long> {
    List<SupportChat> findByStatus(String status);
    List<SupportChat> findByCustomerId(Long customerId);
}
