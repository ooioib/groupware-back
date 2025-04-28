package org.codenova.groupwareback.controller;

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
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/employee")
@CrossOrigin
@RequiredArgsConstructor
@Slf4j
public class EmployeeController {
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final SerialRepository serialRepository;

    // 사원 등록 처리
    @PostMapping
    @Transactional
    public ResponseEntity<Employee> postEmployeeHandle(@ModelAttribute @Valid AddEmployee addEmployee,
                                                       BindingResult result) {

        // 유효성 검증 실패시
        if (result.hasErrors()) {
            // bad request : 서버가 클라이언트 오류를 감지해 요청을 처리할 수 없는 코드
            return ResponseEntity.status(400).body(null);
        }

        // 1. 사원번호 생성, 부서 객체
        // JPA에서 ID로 찾는 것을 기본 제공해주는데, 결과가 Optional 객체가 나옴
        Optional<Serial> serial = serialRepository.findById(1);

        // 정석적으로 사용한다면 serial.isPresent() 확인하여 뽑아 써야함
        Serial found = serial.get(); // 바로 뽑는 이유는 100% 있는 걸 알기 때문

        // 부서 ID를 이용해 부서 조회
        Optional<Department> department = departmentRepository.findById(addEmployee.getDepartmentId());

        // 부서가 없으면 400 Bad Request 반환
        if (department.isEmpty()) {
            return ResponseEntity.status(400).body(null);
        }

        // 2. 사원 객체 생성 및 저장
        Employee employee = Employee.builder()
                .id("g-" + (found.getLastNumber() + 1))                   // 사원 ID 형식 ("g-번호")
                .password(BCrypt.hashpw("0000", BCrypt.gensalt()))  // 초기 비밀번호 "0000" 암호화
                .name(addEmployee.getName())                              // 사원 이름
                .active("N")                                              // 기본 재직 상태는 'N' (미인증/미활성)
                .email(addEmployee.getEmail())                            // 이메일
                .hireDate(addEmployee.getHireDate())                      // 입사일
                .position(addEmployee.getPosition())                      // 직책
                .department(department.get())                             // 부서 연결
                .build();

        // 사원 정보 저장
        employeeRepository.save(employee);

        // 시리얼 테이블의 last_number를 +1 업데이트
        found.setLastNumber(found.getLastNumber() + 1);

        // 변경된 시리얼 정보 저장 (update 쿼리 자동 발생)
        serialRepository.save(found);

        // created : 요청이 성공적으로 처리되었으며, 자원이 생성되었음을 나타내는 성공 상태 응답 코드
        return ResponseEntity.status(201).body(employee);
    }

    // 특정 사원 상세 정보 조회 처리
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeDetailHandle(@PathVariable String id) {

        // 사원 ID로 데이터 조회
        Optional<Employee> employee = employeeRepository.findById(id);

        // 사원이 없으면 404 Not Found 반환
        if (employee.isEmpty()) {
            // not found : 서버가 요청받은 리소스를  찾을 수 없다는 것을 의미
            return ResponseEntity.status(404).body(null);
        }

        // 사원이 존재하면 200 OK 반환 + 사원 데이터 포함
        return ResponseEntity.status(200).body(employee.get());
    }

    /*
    @PostMapping("/verify")
    public ResponseEntity<Employee> postEmployeeVerifyHandle(@RequestParam String id,
                                                             @RequestParam String password) {

        Optional<Employee> employee = employeeRepository.findById(id);

        // 아이디가 없으면 401 에러
        if (employee.isEmpty()) {
            return ResponseEntity.status(401).body(null);
        }

         비밀번호가 없으면 401 에러
        if (!employee.password) {
           return ResponseEntity.status(401).body(null);
        }

        // 아이디, 비밀번호가 있으면 200 OK 반환 + 사원 데이터 포함
        return ResponseEntity.status(200).body(employee.get());
    }

/
     */
}

