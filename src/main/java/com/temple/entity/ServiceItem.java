
package com.temple.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="service_items")
public class ServiceItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String price;
    @Column(length=1000)
    private String description;
    @Column(length=2000)
    private String details;
    @Builder.Default
    private boolean active = true;
}
