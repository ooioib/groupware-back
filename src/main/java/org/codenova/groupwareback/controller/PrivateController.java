package org.codenova.groupwareback.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.codenova.groupwareback.entity.Employee;
import org.codenova.groupwareback.repository.EmployeeRepository;
import org.codenova.groupwareback.request.ChangePassword;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
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
            @RequestHeader("Authorization") @Nullable String token,
            // 요청 본문에서 비밀번호 변경 정보 받음
            @RequestBody @Valid ChangePassword changePassword, BindingResult bindResult) {

        // Authorization 헤더가 없거나 Bearer 형식이 아니면 401
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(null);
        }

        // "Bearer " 접두어 제거해서 실제 토큰만 추출
        token = token.replace("Bearer ", "");
        String subject = null;

        // 토큰 유효성 검증
        try {
            // JWT 검증 객체 생성 (발급자, 비밀 키 설정)
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256("groupware"))
                    .withIssuer("groupware")
                    .build();

            // 토큰 검증
            DecodedJWT jwt = verifier.verify(token);

            // 성공 시 subject (로그인된 사원 ID) 추출
            subject = jwt.getSubject();

            // 검증 실패 → 유효하지 않은 토큰 → 인증 실패 (401)
        } catch (Exception e) {
            return ResponseEntity.status(401).body(null);
        }

        // 요청 데이터가 유효하지 않으면 400 Bad Request
        if (bindResult.hasErrors()) {
            return ResponseEntity.status(400).body(null);
        }

        // 토큰의 소유자와 요청한 employeeId가 다르면 403 Forbidden
        if (!changePassword.getEmployeeId().equals(subject)) {
            return ResponseEntity.status(403).body(null);
        }

        // 해당 ID의 사원을 DB에서 찾음
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

        // active 상태를 Y로 설정 (계정 활성화)
        employee.setActive("Y");

        // 변경된 정보 저장
        employeeRepository.save(employee);

        // 새 데이터가 있는 건 아니므로 203 반환
        return ResponseEntity.status(203).body(null);
    }
}
