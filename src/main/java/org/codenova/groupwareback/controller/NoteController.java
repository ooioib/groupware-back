package org.codenova.groupwareback.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.codenova.groupwareback.entity.Employee;
import org.codenova.groupwareback.entity.Note;
import org.codenova.groupwareback.entity.NoteStatus;
import org.codenova.groupwareback.repository.EmployeeRepository;
import org.codenova.groupwareback.repository.NoteRepository;
import org.codenova.groupwareback.repository.NoteStatusRepository;
import org.codenova.groupwareback.request.AddNote;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/note")
@CrossOrigin
@RequiredArgsConstructor
@Slf4j
public class NoteController {

    // 쪽지 관련 처리를 위한 리포지토리 의존성 주입
    private final EmployeeRepository employeeRepository;
    private final NoteRepository noteRepository;
    private final NoteStatusRepository noteStatusRepository;

    // 쪽지 전송 API
    @PostMapping
    public ResponseEntity<?> createNote(@RequestAttribute String subject,
                                        @RequestBody @Valid AddNote addNote,
                                        BindingResult bindingResult) {

        // 쪽지 내용이 없거나 수신자가 없으면 400 Bad Request
        if (bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "인자누락(내용 필수, 최소 1명 이상 수신사 설정 필수)");
        }

        // 보낸 사람(로그인한 사원) 정보를 DB에서 조회
        Employee subjectEmployee = employeeRepository.findById(subject).orElseThrow(() -> {
            // 사원이 없으면 401 Unauthorized 인증되지 않은 사용자 처리
            return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "미인증 사원");
        });

        // 보낼 쪽지 내용 생성
        Note note = Note.builder()
                .content(addNote.getContent()) // 쪽지 본문
                .sendAt(LocalDateTime.now())   // 보낸 시간은 현재 시간
                .isDelete(false)               // 보낸 쪽지는 기본적으로 삭제되지 않은 상태
                .sender(subjectEmployee)       // 누가 보냈는지 설정
                .build();

        // DB에 쪽지 저장
        noteRepository.save(note);

        // 수신자 ID 리스트를 사원 객체로 변환하며 존재 여부 검증
        List<Employee> receiver = addNote.getReceiverIds().stream().map((id) -> employeeRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "대상이 존재하지 않습니다.")
        )).toList();

        // DB에서 수신자 전체 목록 조회
        List<Employee> receivers = employeeRepository.findAllById(addNote.getReceiverIds());

        // 각 수신자에 대해 NoteStatus 생성 및 저장
        for (Employee e : receivers) {
            NoteStatus noteStatues = NoteStatus.builder()
                    .note(note) // 어떤 쪽지인지 연결
                    .isRead(false) // 아직 읽지 않은 상태
                    .isDelete(false) // 아직 삭제하지 않은 상태
                    .receiver(e) // 이 수신자에게 보내는 상태 정보
                    .build();

            // 쪽지 상태 저장
            noteStatusRepository.save(noteStatues);
        }

        // 처리 성공 203 응답
        return ResponseEntity.status(203).body(null);
    }


    // 받은 쪽지 목록 조회 API
    @GetMapping("/receive")
    public ResponseEntity<?> getReceiveNote(@RequestAttribute String subject) {

        // 로그인한 사원 정보 조회 (없으면 401 Unauthorized)
        Employee subjectEmployee = employeeRepository.findById(subject).orElseThrow(()-> new ResponseStatusException(HttpStatus.UNAUTHORIZED,"미인증 상태"));

        // 받은 쪽지 상태 목록 조회 (NoteStatus 기준)
        List<NoteStatus> noteStatusList = noteStatusRepository.findAllByReceiver(subjectEmployee);

        // 200 OK + 받은 쪽지 상태 리스트 반환
        return ResponseEntity.status(200).body(noteStatusList);
    }


    // 보낸 쪽지 목록 조회 API
    @GetMapping("/send")
    public ResponseEntity<?> getSendNote(@RequestAttribute String subject) {

        // 로그인한 사원 정보 조회 (없으면 401 Unauthorized)
        Employee subjectEmployee = employeeRepository.findById(subject).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "미인증 상태"));

        // 보낸 쪽지 전체 조회
        List<Note> noteList = noteRepository.findAllBySender(subjectEmployee);

        // 200 OK + 보낸 쪽지 목록 반환
        return ResponseEntity.status(200).body(noteList);
    }
}