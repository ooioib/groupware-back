package org.codenova.groupwareback.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AddEmployee {

    @NotBlank  // null 또는 빈 문자열 허용하지 않음 (입력 필수)
    private String name;  // 사원 이름

    @Email     // 이메일 형식인지 검사
    @NotBlank  // 이메일도 필수 입력값
    private String email;  // 사원 이메일

    @PastOrPresent  // 과거 또는 오늘 날짜까지만 허용
    private LocalDate hireDate;  // 입사일

    @NotNull  // 부서 ID는 반드시 있어야 함
    private Integer departmentId;  // 부서 식별자

    private String position;  // 직책 (선택 입력 가능)
}
