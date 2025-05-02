package org.codenova.groupwareback.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.codenova.groupwareback.entity.Employee;
import org.codenova.groupwareback.repository.EmployeeRepository;
import org.codenova.groupwareback.request.ChangePassword;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@CrossOrigin
@RestController
@RequestMapping("/api/private")
@RequiredArgsConstructor
public class PrivateController {

    private final EmployeeRepository employeeRepository;

    // 비밀번호 변경 API
    // REST 설계 원칙에 따라 정보 수정은 PUT 또는 PATCH를 사용 (대부분 PUT 사용)
    @PutMapping("/change-password")
    public ResponseEntity<?> patchChangePasswordHandle(
            // 요청 헤더에서 JWT 토큰 받음
            @RequestAttribute("subject") String subject,
            // 요청 본문에서 비밀번호 변경 정보 받음
            @RequestBody @Valid ChangePassword changePassword, BindingResult bindResult) {

        // 요청 데이터가 유효하지 않으면 400 Bad Request
        if (bindResult.hasErrors()) {
            return ResponseEntity.status(400).body(null);
        }

        // 토큰의 소유자와 요청한 employeeId가 다르면 403 Forbidden
        if (!changePassword.getEmployeeId().equals(subject)) {
            return ResponseEntity.status(403).body(null);
        }

        // 해당 ID의 사원을 DB에서 조회
        Optional<Employee> optionalEmployee = employeeRepository.findById(changePassword.getEmployeeId());

        // 사원이 존재하지 않으면 404 Not Found
        if (optionalEmployee.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }

        Employee employee = optionalEmployee.get();

        // 기존 비밀번호가 DB에 저장된 비밀번호와 다르면 403 Forbidden
        if (!BCrypt.checkpw(changePassword.getOldPassword(), employee.getPassword())) {
            return ResponseEntity.status(403).body(null);
        }

        // 새 비밀번호를 bcrypt로 암호화해서 저장
        employee.setPassword(BCrypt.hashpw(changePassword.getNewPassword(), BCrypt.gensalt()));

        // 계정을 활성화 상태로 설정
        employee.setActive("Y");

        // DB에 변경 사항 저장
        employeeRepository.save(employee);

        // 새 데이터가 있는 건 아니므로 203 반환
        return ResponseEntity.status(203).body(null);
    }
}
