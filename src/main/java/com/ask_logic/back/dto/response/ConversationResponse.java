package com.ask_logic.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ConversationResponse {
    private Long conversationId;
    private String conversationTitle;
    private ArrayList<ChatResponse> messages;
}
