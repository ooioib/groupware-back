package org.codenova.groupwareback.controller;

import lombok.RequiredArgsConstructor;
import org.codenova.groupwareback.entity.Board;
import org.codenova.groupwareback.entity.Employee;
import org.codenova.groupwareback.repository.BoardRepository;
import org.codenova.groupwareback.repository.EmployeeRepository;
import org.codenova.groupwareback.request.AddBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/api/board")
@RequiredArgsConstructor
public class BoardController {

    // 사원과 게시글 정보를 다루기 위한 리포지토리 의존성 주입
    private final EmployeeRepository employeeRepository;
    private final BoardRepository boardRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // 신규 글 등록 API =========================================================
    @PostMapping
    public ResponseEntity<Board> createBoard(@RequestAttribute String subject,
                                             @RequestBody AddBoard addBoard) {

        // subject(로그인한 사원 ID)로 사원 정보 조회
        Optional<Employee> optionalEmployee = employeeRepository.findById(subject);

        // 사원이 없으면 404 Not Found 예외 발생
        Employee employee = optionalEmployee.orElseThrow(() -> {
            return new ResponseStatusException(HttpStatus.NOT_FOUND);
        });

        // 게시글 객체 생성
        Board board = Board.builder()
                .writer(employee)                     // 작성자
                .title(addBoard.getTitle())           // 제목
                .content(addBoard.getContent())       // 내용
                .wroteAt(LocalDateTime.now())         // 작성 시각
                .viewCount(0)                         // 조회수 0으로 초기화
                .build();

        // DB에 게시글 저장
        boardRepository.save(board);

        // /public 채널로 새 글 등록 실시간 알림 전송
        messagingTemplate.convertAndSend("/public", "새 글이 등록되었습니다.");

        // 201 Created 응답 + 저장된 게시글 정보 반환
        return ResponseEntity.status(201).body(board);
    }


    // 전체 글 정보 목록 API ======================================
    @GetMapping
    public ResponseEntity<?> getBoards(@RequestParam(name = "p") Optional<Integer> p) {

        // ID 기준으로 내림차순 정렬하여 모든 게시글 조회
        //  List<Board> boards = boardRepository.findAll(Sort.by("id").descending());

        int pageNumber = p.orElse(1); // int pageNumber p.isPresent() ? p.get() : 0;
        pageNumber = pageNumber < 1 ? 1 : pageNumber;

        // 첫 번째 인자 : 페이지 인덱스
        // 두 번째 인자 : 몇 개씩 페이징 처리할 건지
        Page<Board> boards = boardRepository.findAll(PageRequest.of(pageNumber - 1, 10));

        // 200 OK 응답 + 게시글 리스트 반환
        return ResponseEntity.status(200).body(boards.getContent());
    }


    // 특정 글 정보 API =========================================
    @GetMapping("/{id}")
    public ResponseEntity<Board> getBoardDetailHandle(@PathVariable Long id) {

        // ID로 게시글 조회 (없으면 404 예외 발생)
        Board board = boardRepository.findById(id).orElseThrow(() -> {
            return new ResponseStatusException(HttpStatus.NOT_FOUND);
        });

        // 200 OK 응답 + 게시글 정보 반환
        return ResponseEntity.status(200).body(board);

    }
}
