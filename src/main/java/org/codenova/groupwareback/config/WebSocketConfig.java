package org.codenova.groupwareback.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/*
    WebSocket 통신을 설정하는 클래스
    실시간 알림, 쪽지 읽음 처리, 채팅 등을 위한 STOMP 기반 WebSocket 환경 구성
 */

@Configuration // 설정 클래스임을 명시
@EnableWebSocketMessageBroker // STOMP 메시지 처리 활성화
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // 클라이언트가 연결할 WebSocket 엔드포인트 설정
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/handshake")  // WebSocket 연결 주소
                .setAllowedOrigins("*");           // 모든 도메인에서 접속 허용
    }

    // 메시지 브로커 설정
    // 클라이언트가 해당 경로로 메시지를 받을 수 있게 구독 경로 지정
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // /public 또는 /private로 시작하는 채널 구독 허용
        registry.enableSimpleBroker("/public", "/private", "/chat-department");
    }
}
