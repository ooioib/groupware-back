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
            // 사원이 없으면 인증되지 않은 사용자 처리
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

        // 수신자 ID 리스트를 하나씩 꺼내서 Employee 객체로 바꿈 (사원 존재 확인)
        List<Employee> receiver = addNote.getReceiverIds().stream().map((id) -> employeeRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "대상이 존재하지 않습니다.")
        )).toList();

        // DB에서 수신자 전체 목록 불러오기
        List<Employee> receivers = employeeRepository.findAllById(addNote.getReceiverIds());

        // 수신자 각각에 대해 상태를 따로 저장함
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

    @GetMapping("/receive")
    public ResponseEntity<?> getReceiveNote(@RequestAttribute String subject) {

        // noteStatus 들중에 receiver 가 현재 로그인하고 있는 사용자로 되어있는 데이터만 가져와야함.
        Employee subjectEmployee = employeeRepository.findById(subject).orElseThrow(()-> new ResponseStatusException(HttpStatus.UNAUTHORIZED,"미인증 상태"));

        List<NoteStatus> noteStatusList = noteStatusRepository.findAllByReceiver(subjectEmployee);

        return ResponseEntity.status(200).body(noteStatusList);

    }

    @GetMapping("/send")
    public ResponseEntity<?> getSendNote(@RequestAttribute String subject) {

        Employee subjectEmployee = employeeRepository.findById(subject).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "미인증 상태"));

        List<Note> noteList = noteRepository.findAllBySender(subjectEmployee);

        return ResponseEntity.status(200).body(noteList);
    }
}