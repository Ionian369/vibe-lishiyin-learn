package com.school.reservation.dto;

import com.school.reservation.entity.Reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约记录展示对象
 */
public record ReservationVO(
        Long id,
        Long deviceId,
        String deviceName,
        LocalDate date,
        String period,
        String userName,
        LocalDateTime createdAt) {

    public static ReservationVO from(Reservation reservation, String deviceName) {
        return new ReservationVO(
                reservation.getId(),
                reservation.getDeviceId(),
                deviceName,
                reservation.getDate(),
                reservation.getPeriod(),
                reservation.getUserName(),
                reservation.getCreatedAt());
    }
}
