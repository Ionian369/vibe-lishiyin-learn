package com.school.reservation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约记录：同一器材同一日期同一时段只允许一条
 * 业务层前置校验 + 数据库唯一索引兜底并发
 */
@Getter
@Setter
@Entity
@Table(name = "reservation",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_reservation_slot",
                columnNames = {"device_id", "reservation_date", "period"}))
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 预约的器材 id */
    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    /** 预约日期 */
    @Column(name = "reservation_date", nullable = false)
    private LocalDate date;

    /** 预约时段：上午 / 下午 */
    @Column(nullable = false, length = 10)
    private String period;

    /** 预约人姓名 */
    @Column(name = "user_name", nullable = false, length = 50)
    private String userName;

    /** 创建时间 */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
