package org.codenova.groupwareback.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NoteStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Employee receiver;

    @ManyToOne
    private Note note;

    private Boolean isRead;

    private LocalDateTime readAt;

    private Boolean isDelete;

}
