package com.temple.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="board_members")
public class BoardMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String position;
    private String department;
    private String phoneNumber;
    private String email;

    @Column(length = 3000)
    private String biography;

    @Column(length = 500)
    private String profileImageUrl;

    private String status;
}

