package org.codenova.groupwareback.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codenova.groupwareback.entity.Chat;
import org.codenova.groupwareback.entity.Department;
import org.codenova.groupwareback.entity.Employee;
import org.codenova.groupwareback.repository.ChatRepository;
import org.codenova.groupwareback.repository.DepartmentRepository;
import org.codenova.groupwareback.repository.EmployeeRepository;
import org.codenova.groupwareback.request.AddChat;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@CrossOrigin
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    // 의존성 주입 ====================================================================
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final ChatRepository chatRepository;
    private final SimpMessagingTemplate messagingTemplate;  // STOMP 메시지 전송용(WebSocket)


    // 새 채팅 등록 API ===============================================================
    @PostMapping("/{departmentId}")
    public ResponseEntity<?> postChatHandle(@RequestAttribute String subject,
                                            @RequestBody AddChat addChat,
                                            @PathVariable Integer departmentId) {

        // 로그인한 사용자의 ID(subject)를 기반으로 사원 정보 조회
        Employee subjectEmployee = employeeRepository.findById(subject).orElseThrow();

        // 채팅 대상 부서 ID를 이용해 부서 정보 조회
        Department department = departmentRepository.findById(departmentId).orElseThrow();

        // 채팅 객체 생성
        Chat chat = Chat.builder()
                .talker(subjectEmployee)           // 채팅을 보낸 사람
                .message(addChat.getMessage())     // 입력한 메시지 내용
                .department(department)            // 채팅이 속한 부서
                .build();

        // 생성한 chat 객체를 DB에 저장
        chatRepository.save(chat);

        log.info("새 채팅 등록 요청 처리 완료");

        // WebSocket 브로커를 통해 해당 부서 채팅방 구독자에게 newChat 메시지 전송
        // 프론트엔드에서 이 메시지를 받으면 새 채팅이 등록되었음을 감지하고 채팅 목록 새로고침
        messagingTemplate.convertAndSend("/chat-department/" + departmentId, "newChat");

        // 채팅 생성 성공 응답(201 Created) + 채팅 객체 반환
        return ResponseEntity.status(201).body(chat);
    }

    // 해당 부서의 채팅 리스트 =========================================================
    @GetMapping("/{departmentId}")
    public ResponseEntity<?> getChatList(@PathVariable Integer departmentId) {

        // 부서 ID를 기반으로 부서 정보 조회
        Department department = departmentRepository.findById(departmentId).orElseThrow();

        // 해당 부서의 채팅 목록을 ID 순서대로 조회
        List<Chat> chatList = chatRepository.findAllByDepartmentOrderById(department);

        // 성공 200 ok + 조회된 채팅 목록 반환
        return ResponseEntity.status(200).body(chatList);
    }
}
