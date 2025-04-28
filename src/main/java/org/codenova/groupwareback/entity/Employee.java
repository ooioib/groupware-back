package org.codenova.groupwareback.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.time.LocalDate;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    private String id;

    private String password;

    private String name;

    private String email;

    private LocalDate hireDate;

    @ManyToOne   // 다대일 관계
    @JoinColumn(name = "department_id")
    private Department department;

    private String position;

    private  String active;
}
