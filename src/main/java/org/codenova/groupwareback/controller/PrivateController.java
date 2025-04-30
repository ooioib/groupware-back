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

    // 수정하는 API는 put or patch mapping 을 사용하는 게 설계원칙에 맞음. (용도는 다름)
    // 우리나라 개발자들은 보통은 put으로 처리를 하는 경우가 많음.
    @PutMapping("/change-password")
    public ResponseEntity<?> patchChangePasswordHandle(@RequestBody @Valid ChangePassword changePassword, BindingResult bindResult) {

        // if 바인딩리절트 에러 있으면 400번 응답
        if (bindResult.hasErrors()) {
            return ResponseEntity.status(400).body(null);
        }

        Optional<Employee> optionalEmployee = employeeRepository.findById(changePassword.getEmployeeId());

        // employeeId 로 employee 찾앗는데 없으면 404 응답
        if (optionalEmployee.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }

        Employee employee = optionalEmployee.get();

        // 찾았는데 데이터베이스에 저장된 비밀번호와 oldPassword가 다르면 401
        if (!BCrypt.checkpw(changePassword.getOldPassword(), employee.getPassword())) {
            return ResponseEntity.status(401).body(null);
        }

        // 여기까지가 통과됬으면 찾은 객체에다가 password를 세팅시켜 (사용자가 보내준 newPassword 를 bcrypt로 암호화해서)
        employee.setPassword(BCrypt.hashpw(changePassword.getNewPassword(), BCrypt.gensalt()));

        //  active도 Y로 세팅시켜
        employee.setActive("Y");

        // 객체 save 시키고
        employeeRepository.save(employee);

        // 200 or 203 응답
        return ResponseEntity.status(203).body(null);
    }


}