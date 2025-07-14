package com.ask_logic.back.repository;

import com.ask_logic.back.domain.Conversation;
import com.ask_logic.back.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    ArrayList<Conversation> findAllByUser(User user);
}
