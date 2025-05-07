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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    // 쪽지 전송 API ===========================================================
    @PostMapping
    public ResponseEntity<?> createNote(@RequestAttribute String subject,
                                        @RequestBody @Valid AddNote addNote,
                                        BindingResult bindingResult) {

        // 쪽지 내용이 없거나 수신자가 없으면 400 Bad Request
        if (bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "인자누락(내용 필수, 최소 1명 이상 수신사 설정 필수)"
            );
        }

        // 보낸 사람(로그인한 사원) 정보를 DB에서 조회
        Employee subjectEmployee = employeeRepository
                .findById(subject)
                .orElseThrow(() -> {
                    // 사원이 없으면 401 Unauthorized 인증되지 않은 사용자 처리
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "미인증 사원");
                });

        // 보낼 쪽지 내용 객체 생성
        Note note = Note.builder()
                .content(addNote.getContent()) // 쪽지 본문
                .sendAt(LocalDateTime.now())   // 보낸 시간은 현재 시간
                .isDelete(false)               // 보낸 쪽지는 기본적으로 삭제되지 않은 상태
                .sender(subjectEmployee)       // 누가 보냈는지 설정
                .build();

        // DB에 쪽지 저장
        noteRepository.save(note);

        // 수신자 ID 리스트를 사원 객체로 변환하며 존재 여부 검증
        List<Employee> receiver = addNote
                .getReceiverIds()
                .stream()
                .map((id) -> employeeRepository
                        .findById(id)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "대상이 존재하지 않습니다.")
                        ))
                .toList();

        // DB에서 수신자 전체 목록 조회
        List<Employee> receivers = employeeRepository.findAllById(addNote.getReceiverIds());

        // 각 수신자에 대해 NoteStatus 생성 및 저장
        for (Employee e : receivers) {
            NoteStatus noteStatues = NoteStatus.builder()
                    .note(note)      // 어떤 쪽지인지 연결
                    .isRead(false)   // 아직 읽지 않은 상태
                    .isDelete(false) // 아직 삭제하지 않은 상태
                    .receiver(e)     // 이 수신자에게 보내는 상태 정보
                    .build();

            // 쪽지 상태 저장
            noteStatusRepository.save(noteStatues);
        }

        // 처리 성공 203 응답
        return ResponseEntity.status(203).body(null);
    }


    // 받은 쪽지 목록 조회 API ===========================================================
    @GetMapping("/inBox")
    public ResponseEntity<?> getReceiveNote(@RequestAttribute String subject) {

        // 로그인한 사원 정보 조회 (없으면 401 Unauthorized)
        Employee subjectEmployee = employeeRepository
                .findById(subject)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "미인증 상태")
                );

        // 받은 쪽지 상태 목록 조회 (NoteStatus 기준)
        List<NoteStatus> noteStatusList = noteStatusRepository.findAllByReceiver(subjectEmployee);

        // 200 OK + 받은 쪽지 상태 리스트 반환
        return ResponseEntity.status(200).body(noteStatusList);
    }


    // 보낸 쪽지 목록 조회 API ===========================================================
    @GetMapping("/outBox")
    public ResponseEntity<?> getSendNote(@RequestAttribute String subject) {

        // 로그인한 사원 정보 조회 (없으면 401 Unauthorized)
        Employee subjectEmployee = employeeRepository
                .findById(subject)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "미인증 상태")
                );

        // 보낸 쪽지들 전체 조회 (Note 테이블에서 sender가 나인 것들)
        List<Note> sendNotes = noteRepository.findAllBySender(subjectEmployee);

        // 해당 쪽지들에 대한 상태 정보들 조회 (NoteStatus 테이블에서 Note가 포함된 것들)
        List<NoteStatus> sendNoteStatus = noteStatusRepository.findAllByNoteIn(sendNotes);

        // 200 OK + 보낸 쪽지 목록 반환
        return ResponseEntity.status(200).body(sendNoteStatus);
    }


    // 쪽지 상태 변경 API ===========================================================
    @PutMapping("/status/{id}")
    public ResponseEntity<?> putStatusHandle(@RequestAttribute String subject,
                                             @PathVariable Long id) {

        // 전달받은 쪽지 상태 ID로 해당 NoteStatus 조회
        Optional<NoteStatus> optionalNoteStatus = noteStatusRepository.findById(id);

        // 존재하지 않는 ID일 경우 404 에러
        if (optionalNoteStatus.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "잘못된 id 값이 전달 되었습니다.");
        }

        // NoteStatus 가져오기
        NoteStatus noteStatus = optionalNoteStatus.get();

        // 본인이 받은 쪽지가 아닌 경우 403 에러
        if (!noteStatus.getReceiver().getId().equals(subject)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "자신이 받은 쪽지만 상태 변경 가능");
        }

        // 아직 읽지 않은 쪽지일 경우
        if (noteStatus.getIsRead()) {
            // 읽음 처리
            noteStatus.setIsRead(true);
            noteStatus.setReadAt(LocalDateTime.now());
            // 변경 내용 저장
            noteStatusRepository.save(noteStatus);
        }

        // 200 성공 응답 반환
        return ResponseEntity.status(200).body(noteStatus);
    }
}