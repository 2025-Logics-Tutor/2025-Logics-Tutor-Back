package com.ask_logic.back.service;

import com.ask_logic.back.domain.Conversation;
import com.ask_logic.back.domain.Message;
import com.ask_logic.back.domain.User;
import com.ask_logic.back.dto.request.ChatRequest;
import com.ask_logic.back.dto.response.ChatResponse;
import com.ask_logic.back.dto.response.ConversationResponse;
import com.ask_logic.back.dto.response.ConversationsResponse;
import com.ask_logic.back.repository.ConversationRepository;
import com.ask_logic.back.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public ArrayList<ConversationsResponse> getConversations(User user) {
        ArrayList<Conversation> conversations = conversationRepository.findAllByUser(user);
        ArrayList<ConversationsResponse> responses = new ArrayList<>();
        for (Conversation conversation : conversations)
            responses.add(new ConversationsResponse(
                    conversation.getId(),
                    conversation.getTitle()
            ));
        return responses;
    }

    public ConversationResponse getConversation(User user, Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
        ArrayList<Message> messages = messageRepository.findAllByConversation(conversation);
        if (conversation == null || conversation.getUser() != user)
            return null;
        ArrayList<ChatResponse> chatResponses = new ArrayList<>();
        for (Message message : messages)
            chatResponses.add(new ChatResponse(
                    message.getId(),
                    message.getRole(),
                    message.getContent(),
                    message.getCreatedAt()
            ));
        ConversationResponse response = new ConversationResponse(
                conversation.getId(),
                conversation.getTitle(),
                chatResponses
        );
        return response;
    }

    public ConversationResponse generateConversation(User user, ChatRequest request) {
        log.info(request.toString());
        String question = request.getMessage();

        WebClient client = WebClient.builder().build();

        Map<String, String> reqBody = Map.of("question", question);

        // AI 서버로 요청
        Map<String, String> aiResponse = client.post()
                .uri("http://localhost:8000/api/ask/new")
                .header("Content-Type", "application/json")
                .bodyValue(reqBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {})
                .block();

        String title = aiResponse.get("title");
        String answer = aiResponse.get("answer");

        // 대화 및 메시지 저장
        Conversation conversation = Conversation.builder()
                .user(user)
                .title(title)
                .build();
        conversationRepository.save(conversation);

        Message userMsg = Message.builder()
                .conversation(conversation)
                .role("USER")
                .content(question)
                .build();

        Message assistantMsg = Message.builder()
                .conversation(conversation)
                .role("ASSISTANT")
                .content(answer)
                .build();

        messageRepository.save(userMsg);
        messageRepository.save(assistantMsg);

        // 응답용 DTO
        ArrayList<ChatResponse> messages = new ArrayList<>();
        messages.add(new ChatResponse(
                userMsg.getId(), "USER", userMsg.getContent(), userMsg.getCreatedAt()
        ));
        messages.add(new ChatResponse(
                assistantMsg.getId(), "ASSISTANT", assistantMsg.getContent(), assistantMsg.getCreatedAt()
        ));

        return new ConversationResponse(conversation.getId(), conversation.getTitle(), messages);
    }

    public ChatResponse chat(User user, Long conversationId, ChatRequest request) {
        log.info(user.toString());
        log.info(conversationId.toString());
        log.info(request.toString());
        String question = request.getMessage();
        log.info("📩 받은 질문: {}", question);

        try {
            WebClient client = WebClient.builder().build();

            Flux<String> responseStream = client.post()
                    .uri("http://localhost:8000/api/ask/stream")
                    .header("Content-Type", "application/json")
                    .bodyValue(Map.of("question", question))
                    .retrieve()
                    .bodyToFlux(String.class)
                    .doOnSubscribe(sub -> log.info("🚀 AI 서버 호출 시작"))
                    .doOnNext(token -> log.info("🔹 응답 토큰 수신: {}", token))
                    .doOnError(e -> log.error("🔥 WebClient 오류 발생", e))
                    .doOnComplete(() -> log.info("✅ GPT 응답 완료"));

            StringBuilder answerBuilder = new StringBuilder();

            responseStream
                    .doOnNext(answerBuilder::append)
                    .blockLast(); // 💥 이 줄에서 죽었을 가능성 높음

            String answer = answerBuilder.toString();
            log.info("💬 최종 응답: {}", answer);

            return new ChatResponse(null, "ASSISTANT", answer, null);
        } catch (Exception e) {
            log.error("🔥 전체 chat() 흐름에서 예외 발생", e);
            return null;
        }
    }

    public void deleteConversation(User user, Long conversationId) {
    }
}
