package com.school.reservation.dto;

import com.school.reservation.entity.Reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 预约记录展示对象
 *
 * @param status 状态（后端为唯一裁判：date < 服务器今天 → 已结束，否则 → 待使用）
 */
public record ReservationVO(
        Long id,
        Long deviceId,
        String deviceName,
        LocalDate date,
        String period,
        String userName,
        String status,
        LocalDateTime createdAt) {

    public static ReservationVO from(Reservation reservation, String deviceName) {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        String status = reservation.getDate().isBefore(today) ? "已结束" : "待使用";
        return new ReservationVO(
                reservation.getId(),
                reservation.getDeviceId(),
                deviceName,
                reservation.getDate(),
                reservation.getPeriod(),
                reservation.getUserName(),
                status,
                reservation.getCreatedAt());
    }
}
