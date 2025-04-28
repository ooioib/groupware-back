package org.codenova.groupwareback.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="department")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @Column(name="name")
    private String name;
}
