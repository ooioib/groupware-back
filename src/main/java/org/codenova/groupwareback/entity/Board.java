package org.codenova.groupwareback.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne // 다대일 관계
    @JoinColumn(name = "writer_id")
    private Employee writer;

    private String title;

    private String content;

    private Integer viewCount;

    private LocalDateTime wroteAt;

}
