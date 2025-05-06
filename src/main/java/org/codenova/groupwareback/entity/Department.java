package org.codenova.groupwareback.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity                     // JPA가 관리하는 엔티티 클래스임을 명시
@Table(name = "department") // 매핑될 테이블 이름 지정
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id  // primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // AUTO_INCREMENT 방식으로 ID 자동 생성
    @Column(name = "id")  // 실제 테이블의 컬럼 이름
    private Integer id;   // 부서 고유 번호

    @Column(name = "name")  // 테이블의 name 컬럼과 매핑
    private String name;    // 부서 이름
}
