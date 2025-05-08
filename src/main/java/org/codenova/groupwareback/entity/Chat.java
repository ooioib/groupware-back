package org.codenova.groupwareback.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity  // JPA가 관리하는 엔티티 클래스임을 명시
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT 전략 (DB가 자동 생성)
    private Long id;

    @ManyToOne
    private Department department;

    @ManyToOne
    private Employee talker;

    private String message;

}
