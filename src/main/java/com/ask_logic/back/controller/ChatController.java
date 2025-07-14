package com.ask_logic.back.controller;

import com.ask_logic.back.domain.User;
import com.ask_logic.back.dto.request.ChatRequest;
import com.ask_logic.back.dto.response.ChatResponse;
import com.ask_logic.back.dto.response.ConversationResponse;
import com.ask_logic.back.dto.response.ConversationsResponse;
import com.ask_logic.back.global.ApiResponse;
import com.ask_logic.back.jwt.JwtUtil;
import com.ask_logic.back.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/conversations")
@Slf4j
public class ChatController {
    private final JwtUtil jwtUtil;
    private final ChatService chatService;

    // 사이드바에서 대화 내역 조회
    @GetMapping("")
    public ResponseEntity<ApiResponse<?>> getConversations(@RequestHeader("Authorization") String token) {
        User user = jwtUtil.getUser(token);
        ArrayList<ConversationsResponse> responses = chatService.getConversations(user);
        return (responses != null)?
                ResponseEntity.ok(ApiResponse.success(responses)):
                ResponseEntity.internalServerError().body(ApiResponse.error(500, "대화 조회에 실패했습니다."));
    }

    // 특정 대화 조회
    @GetMapping("/{conversationId}")
    public ResponseEntity<ApiResponse<?>> getConversation(@PathVariable("conversationId") Long conversationId,
                                                          @RequestHeader("Authorization") String token){
        User user = jwtUtil.getUser(token);
        ConversationResponse response = chatService.getConversation(user, conversationId);
        return (response != null)?
                ResponseEntity.ok(ApiResponse.success(response)):
                ResponseEntity.internalServerError().body(ApiResponse.error(500, "대화 내역 조회에 실패했습니다."));
    }

    // 새 대화 생성
    @PostMapping("")
    public ResponseEntity<ApiResponse<?>> generateConversation(@RequestHeader("Authorization") String token,
                                                               @RequestBody @Valid ChatRequest request){
        log.info(token);
        User user = jwtUtil.getUser(token);
        log.info(user.toString());
        ConversationResponse response = chatService.generateConversation(user, request);
        return (response != null)?
                ResponseEntity.ok(ApiResponse.success(response)):
                ResponseEntity.internalServerError().body(ApiResponse.error(500, "대화 생성에 실패했습니다."));
    }


    // 이미 있는 대화에 메시지 보내기
    @PostMapping("/{conversationId}/chat")
    public ResponseEntity<ApiResponse<?>> chat(@PathVariable("conversationId") Long conversationId,
                                               @RequestHeader("Authorization") String token,
                                               @RequestBody @Valid ChatRequest request) {
        log.info("🔐 Authorization Header: '{}'", token);
        try {
            User user = jwtUtil.getUser(token);
            ChatResponse response = chatService.chat(user, conversationId, request);
            return (response != null) ?
                    ResponseEntity.ok(ApiResponse.success(response)) :
                    ResponseEntity.internalServerError().body(ApiResponse.error(500, "대화 생성에 실패했습니다."));
        } catch (Exception e) {
            log.error("❌ [Controller] 예외 발생", e);
            return ResponseEntity.internalServerError().body(ApiResponse.error(500, "토큰 파싱 실패 또는 내부 오류"));
        }
//        ChatResponse response = chatService.chat(user, conversationId, request);
//        return (response != null)?
//                ResponseEntity.ok(ApiResponse.success(response)):
//                ResponseEntity.internalServerError().body(ApiResponse.error(500, "대화 생성에 실패했습니다."));
    }

    // 대화 삭제
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<ApiResponse<?>> deleteConversation(@PathVariable("conversationId") Long conversationId,
                                                             @RequestHeader("Authorization") String token) {
        User user = jwtUtil.getUser(token);
        chatService.deleteConversation(user, conversationId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
