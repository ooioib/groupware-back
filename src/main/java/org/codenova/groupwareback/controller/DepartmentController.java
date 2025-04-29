package org.codenova.groupwareback.controller;

import lombok.RequiredArgsConstructor;
import org.codenova.groupwareback.entity.Department;
import org.codenova.groupwareback.repository.DepartmentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin      // 프론트엔드 요청 허용 (CORS 허용)
@RestController   // REST API를 제공하는 컨트롤러
@RequestMapping("/api/department")   // 공통 URL 경로
@RequiredArgsConstructor
public class DepartmentController {

    // 부서 정보 조회에 사용할 리포지토리 객체
    private final DepartmentRepository departmentRepository;

    // ===========================================================
    // @RequestMapping("/api")일 경우,
    // @GetMapping("/department")으로 요청 처리
    @GetMapping
    // 데이터를 HTTP 응답 바디에 담아 클라이언트에 전송
    public ResponseEntity<List<Department>> getDepartmentHandle() {

        // 모든 부서 데이터 조회
        List<Department> list = departmentRepository.findAll();

        // 조회한 부서 리스트를 HTTP 응답(200 OK)으로 반환
        // 200 OK는 요청이 성공했음을 나타내는 성공 응답 상태 코드
        return ResponseEntity.status(200).body(list);
    }
}
