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

    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    @PastOrPresent
    private LocalDate hireDate;

    @NotNull
    private Integer departmentId;

    private String position;
}
