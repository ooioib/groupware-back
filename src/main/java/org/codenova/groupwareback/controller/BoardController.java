package org.codenova.groupwareback.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.codenova.groupwareback.entity.Board;
import org.codenova.groupwareback.entity.Employee;
import org.codenova.groupwareback.repository.BoardRepository;
import org.codenova.groupwareback.repository.EmployeeRepository;
import org.codenova.groupwareback.request.AddBoard;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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
    @Transactional
    public ResponseEntity<Board> postBoardHandle(@RequestBody @Valid AddBoard addBoard,
                                                 BindingResult result) {

        // 유효성 검증
        if (result.hasErrors()) {
            return ResponseEntity.status(400).body(null);
        }

        // 사원 조회
        Optional<Employee> employee = employeeRepository.findById(addBoard.getEmployeeId());

        // 사원이 없으면 400 Bad Request
        if (employee.isEmpty()) {
            return ResponseEntity.status(400).body(null);
        }

        // 게시글 객체 생성
        Board board = Board.builder()
                .writer(employee.get())
                .title(addBoard.getTitle())
                .content(addBoard.getContent())
                .viewCount(0)
                .wroteAt(LocalDateTime.now())
                .build();

        // DB에 게시글 저장
        boardRepository.save(board);

        return ResponseEntity.status(203).body(board);
    }

    // 전체 글 정보 목록 API ======================================
    @GetMapping
    public ResponseEntity<List<Board>> getBoardHandel() {

        // 모든 글 목록 조회
        List<Board> list = boardRepository.findAll();

        return ResponseEntity.status(200).body(list);

    }


    // 특정 글 정보 API =========================================
    @GetMapping("/{id}")
    public ResponseEntity<Board> getBoardDetailHandle(@PathVariable Long id) {

        // 게시글 Id로 조회
        Optional<Board> board = boardRepository.findById(id);

        // 글이 없으면 404 Not Found
        if (board.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }

        // 있으면 200 OK
        return ResponseEntity.status(200).body(board.get());

    }
}
