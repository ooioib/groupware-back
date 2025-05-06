package org.codenova.groupwareback.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity  // JPA가 관리하는 엔티티 클래스임을 명시
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Board {

    @Id  // primary key 필드
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT 전략 (DB가 자동 생성)
    private Long id;  // 게시글 고유 ID

    @ManyToOne  // 다대일 관계 : 여러 게시글이 한 명의 작성자(Employee)에 속함
    @JoinColumn(name = "writer_id") // 외래 키 컬럼 이름 설정 (DB에 생성될 실제 컬럼명)
    private Employee writer;  // 게시글 작성자 정보

    private String title;     // 게시글 제목

    private String content;   // 게시글 본문 내용

    private Integer viewCount;  // 조회수

    private LocalDateTime wroteAt;  // 작성 시각
}
