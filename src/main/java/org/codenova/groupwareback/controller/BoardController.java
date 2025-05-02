package org.codenova.groupwareback.controller;

import lombok.RequiredArgsConstructor;
import org.codenova.groupwareback.entity.Board;
import org.codenova.groupwareback.entity.Employee;
import org.codenova.groupwareback.repository.BoardRepository;
import org.codenova.groupwareback.repository.EmployeeRepository;
import org.codenova.groupwareback.request.AddBoard;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    private final EmployeeRepository employeeRepository;
    private final BoardRepository boardRepository;

    // 신규 글 등록 API =========================================================
    @PostMapping
    public ResponseEntity<Board> createBoard(@RequestAttribute String subject,
                                             @RequestBody AddBoard addBoard) {

        // 사원 조회
        Optional<Employee> optionalEmployee = employeeRepository.findById(subject);

        // 없을 때 예외 터뜨림
        Employee employee = optionalEmployee.orElseThrow(() -> {
            return new ResponseStatusException(HttpStatus.NOT_FOUND);
        });

        // 게시글 객체 생성
        Board board = Board.builder()
                .writer(employee)
                .title(addBoard.getTitle())
                .content(addBoard.getContent())
                .wroteAt(LocalDateTime.now())
                .viewCount(0)
                .build();

        // DB에 게시글 저장
        boardRepository.save(board);

        return ResponseEntity.status(201).body(board);
    }

    // 전체 글 정보 목록 API ======================================
    @GetMapping
    public ResponseEntity<List<Board>> getBoards() {

        // 모든 글 목록 조회
        List<Board> boards = boardRepository.findAll(Sort.by("id").descending());

        return ResponseEntity.status(200).body(boards);

    }


    // 특정 글 정보 API =========================================
    @GetMapping("/{id}")
    public ResponseEntity<Board> getBoardDetailHandle(@PathVariable Long id) {

        Board board = boardRepository.findById(id).orElseThrow(() -> {
            return new ResponseStatusException(HttpStatus.NOT_FOUND);
        });

        // 있으면 200 OK
        return ResponseEntity.status(200).body(board);

    }
}
