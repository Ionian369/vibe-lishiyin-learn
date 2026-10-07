package com.school.reservation.repository;

import com.school.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /** 冲突校验：同器材同日期同时段是否已被预约 */
    boolean existsByDeviceIdAndDateAndPeriod(Long deviceId, LocalDate date, String period);

    /** 我的预约成功次数 */
    long countByUserName(String userName);

    /** 我的预约历史（按提交时间倒序） */
    List<Reservation> findByUserNameOrderByCreatedAtDesc(String userName);
}
