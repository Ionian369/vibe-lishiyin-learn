package com.school.reservation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 可预约器材
 */
@Getter
@Setter
@Entity
@Table(name = "device")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 器材名称，如：投影仪 */
    @Column(nullable = false, length = 50)
    private String name;

    /** 器材描述 */
    @Column(length = 200)
    private String description;
}
