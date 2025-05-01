package org.codenova.groupwareback.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codenova.groupwareback.entity.Department;
import org.codenova.groupwareback.entity.Employee;
import org.codenova.groupwareback.entity.Serial;
import org.codenova.groupwareback.repository.DepartmentRepository;
import org.codenova.groupwareback.repository.EmployeeRepository;
import org.codenova.groupwareback.repository.SerialRepository;
import org.codenova.groupwareback.request.AddEmployee;
import org.codenova.groupwareback.request.Login;
import org.codenova.groupwareback.response.LoginResult;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/employee")
@CrossOrigin
@RequiredArgsConstructor
@Slf4j
public class EmployeeController {

    // 의존성 주입
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final SerialRepository serialRepository;

    @Value("${secret}")   // springframwork 패키지의 value 어노테이션
    private String secret;

    // 사원 전체 조회 API ========================================================
    @GetMapping
    public ResponseEntity<List<Employee>> getEmployeeHandle() {

        // 모든 사원 목록 조회
        List<Employee> list = employeeRepository.findAll();

        // 200 OK와 함께 사원 리스트 반환
        return ResponseEntity.status(200).body(list);
    }

    // 사원 등록 API ===========================================================
    @PostMapping
    @Transactional
    public ResponseEntity<Employee> postEmployeeHandle(@RequestBody @Valid AddEmployee addEmployee,
                                                       BindingResult result) {

        // 유효성 검증 (실패시 400 bad request)
        if (result.hasErrors()) {
            // 400 서버가 클라이언트 오류를 감지해 요청을 처리할 수 없는 코드
            return ResponseEntity.status(400).body(null);
        }

        // 1. 사원번호 생성, 부서 객체
        // 사원 ID 생성을 위한 시리얼 번호 조회
        // JPA에서는 ID나 ref로 조회하면 Optional 타입으로 반환됨
        Optional<Serial> serial = serialRepository.findByRef("employee");

        // 정석적으로는 serial.isPresent() 체크해야 하지만 무조건 있다고 가정하고 바로 꺼냄
        Serial found = serial.get();

        // 입력된 부서 ID로 부서 조회
        Optional<Department> department = departmentRepository.findById(addEmployee.getDepartmentId());

        // 부서가 없으면 400 Bad Request 반환
        if (department.isEmpty()) {
            return ResponseEntity.status(400).body(null);
        }

        // 2. 사원 객체 생성 및 저장
        Employee employee = Employee.builder()
                .id("g-" + (found.getLastNumber() + 1))                      // 사원 ID : g-번호
                .password(BCrypt.hashpw("0000", BCrypt.gensalt()))   // 초기 비밀번호 0000 암호화
                .name(addEmployee.getName())                                 // 사원 이름
                .active("N")                                                 // 기본 재직 상태 N
                .email(addEmployee.getEmail())                               // 이메일
                .hireDate(addEmployee.getHireDate())                         // 입사일
                .position(addEmployee.getPosition())                         // 직책
                .department(department.get())                                // 부서 연결
                .build();

        // DB에 사원 정보 저장
        employeeRepository.save(employee);

        // 사원번호 시리얼 last_number +1 증가
        found.setLastNumber(found.getLastNumber() + 1);

        // 업데이트 된 시리얼 정보 저장 (update 쿼리 자동 발생)
        serialRepository.save(found);

        //  저장된 사원 정보 포함하여 201 Created 반환
        // 201 created : 요청이 성공적으로 처리되었으며, 자원이 생성되었음을 나타내는 성공 상태 응답 코드
        return ResponseEntity.status(201).body(employee);
    }


    // 로그인 인증 API ===========================================================
    @PostMapping("/verify")
    public ResponseEntity<LoginResult> verifyHandle(@RequestBody @Valid Login login,
                                          BindingResult result) {

        // 요청 데이터가 유효하지 않으면 400 Bad Request
        if (result.hasErrors()) {
            return ResponseEntity.status(400).body(null);
        }

        // 사원 ID로 조회
        Optional<Employee> employee = employeeRepository.findById(login.getId());

        // 사원이 없거나 비밀번호가 틀리면 401 Unauthorized
        if (employee.isEmpty() || !BCrypt.checkpw(login.getPassword(), employee.get().getPassword())) {
            return ResponseEntity.status(401).body(null);
        }

        // JWT 토큰 생성
        String token = JWT.create()
                .withIssuer("groupware")              // 발급자 정보
                .withSubject(employee.get().getId())  // subject에 사원 ID 저장
                .sign(Algorithm.HMAC256(secret));     // 비밀키로 서명

        // 응답 객체 생성
        LoginResult loginResult = LoginResult.builder()  // LoginResult 객체를 빌더 패턴으로 생성 시작
                .token(token)                            // JWT 토큰을 응답에 포함시킴
                .employee(employee.get())                // 로그인한 사원의 전체 정보를 포함시킴
                .build();                                // 최종적으로 LoginResult 객체 완성

        // 200 OK + 로그인 결과 반환
        return ResponseEntity.status(200).body(loginResult);
    }


    // 특정 사원 상세 정보 조회 API ========================================================
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeDetailHandle(@PathVariable String id) {

        // 사원 ID로 데이터 조회
        Optional<Employee> employee = employeeRepository.findById(id);

        // 사원이 없으면 404 Not Found 반환
        if (employee.isEmpty()) {
            // 404 not found : 서버가 요청받은 리소스를  찾을 수 없다는 것을 의미
            return ResponseEntity.status(404).body(null);
        }

        // 사원이 존재하면 200 OK 반환 + 사원 정보 포함
        return ResponseEntity.status(200).body(employee.get());
    }
}


