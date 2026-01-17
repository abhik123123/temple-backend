package com.temple.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="board_member_services")
public class BoardMemberService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "board_member_id", nullable = false)
    private BoardMember boardMember;

    private String serviceTitle;

    @Column(length = 1000)
    private String serviceDescription;

    private LocalDate serviceStartDate;

    private LocalDate serviceEndDate;

    private String status; // Active, Inactive, Completed

    private String remarks;
}

