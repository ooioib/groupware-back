package org.codenova.groupwareback.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codenova.groupwareback.entity.Chat;
import org.codenova.groupwareback.entity.Department;
import org.codenova.groupwareback.entity.Employee;
import org.codenova.groupwareback.repository.ChatRepository;
import org.codenova.groupwareback.repository.DepartmentRepository;
import org.codenova.groupwareback.repository.EmployeeRepository;
import org.codenova.groupwareback.request.AddChat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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


    // 새 채팅 등록 API ===============================================================
    @PostMapping("/{departmentId}")
    public ResponseEntity<?> postChatHandle(@RequestAttribute String subject,
                                            @RequestBody AddChat addChat,
                                            @PathVariable Integer departmentId) {

        Employee subjectEmployee = employeeRepository.findById(subject).orElseThrow();
        Department department = departmentRepository.findById(departmentId).orElseThrow();

        Chat chat = Chat.builder()
                .talker(subjectEmployee)
                .message(addChat.getMessage())
                .department(department)
                .build();

        chatRepository.save(chat);

        return ResponseEntity.status(201).body(chat);
    }

    // 해당 부서의 채팅 리스트 =========================================================
    @GetMapping("/{departmentId}")
    public ResponseEntity<?> getChatList(@PathVariable Integer departmentId) {

        Department department = departmentRepository.findById(departmentId).orElseThrow();

        List<Chat> chatList = chatRepository.findAllByDepartmentOrderById(department);

        return ResponseEntity.status(200).body(chatList);
    }
}


