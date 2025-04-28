package org.codenova.groupwareback.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="serial")  // 엔티티명과 테이블명이 일치하면 생략 가능
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Serial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")       // 컬럼명과 필드명이 일치하면 생략
    private Integer id;

    private String ref;

    // 생략시엔 카멜 필드의 경우 자동으로 underscore 형태로 연결됨
    private Long lastNumber;
}

