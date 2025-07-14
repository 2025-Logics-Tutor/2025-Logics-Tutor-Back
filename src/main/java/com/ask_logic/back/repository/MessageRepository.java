package com.ask_logic.back.repository;

import com.ask_logic.back.domain.Conversation;
import com.ask_logic.back.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    ArrayList<Message> findAllByConversation(Conversation conversation);
}
