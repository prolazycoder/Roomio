package com.hostelapp.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "rooms")
@Getter
@Setter
public class Room extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_number", nullable = false)
    private String roomNumber;

    @Column(name = "total_beds", nullable = false)
    private Integer totalBeds;

    @Column(name = "vacant_beds", nullable = false)
    private Integer vacantBeds;

    @Column(name = "price_per_month", nullable = false)
    private BigDecimal pricePerMonth;
}
