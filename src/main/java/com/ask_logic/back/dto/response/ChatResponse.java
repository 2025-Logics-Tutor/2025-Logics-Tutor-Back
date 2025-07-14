package com.ask_logic.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChatResponse {
    private Long messageId;
    private String role;         // "user" 또는 "assistant"
    private String content;      // 메시지 본문
    private LocalDateTime createdAt;    // ISO 8601 형식의 날짜 (예: "2025-07-14T14:33:20Z")
}
