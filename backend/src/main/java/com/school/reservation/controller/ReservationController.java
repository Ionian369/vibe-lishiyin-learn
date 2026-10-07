package com.school.reservation.controller;

import com.school.reservation.dto.ReservationRequest;
import com.school.reservation.dto.ReservationVO;
import com.school.reservation.dto.Result;
import com.school.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 预约提交与统计
 */
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * POST /api/reservations：提交预约
     * 冲突时返回 HTTP 409，message 即冲突原因（前端直接展示，不自行编造）
     */
    @PostMapping
    public Result<ReservationVO> create(@Valid @RequestBody ReservationRequest request) {
        return Result.ok(reservationService.create(request));
    }

    /** GET /api/reservations?user=xxx：我的预约历史列表（按提交时间倒序） */
    @GetMapping
    public Result<List<ReservationVO>> list(@RequestParam String user) {
        return Result.ok(reservationService.listByUser(user));
    }

    /** GET /api/reservations/stats?user=xxx：我的预约成功次数 */
    @GetMapping("/stats")
    public Result<Map<String, Long>> stats(@RequestParam String user) {
        return Result.ok(Map.of("count", reservationService.countByUser(user)));
    }

    /** DELETE /api/reservations/{id}?user=xxx：取消预约（仅本人，404/403 由 Service 判定） */
    @DeleteMapping("/{id}")
    public Result<Void> cancel(@PathVariable Long id, @RequestParam String user) {
        reservationService.cancel(id, user);
        return Result.ok(null);
    }
}
