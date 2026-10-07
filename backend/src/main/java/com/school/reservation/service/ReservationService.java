package com.school.reservation.service;

import com.school.reservation.dto.PageVO;
import com.school.reservation.dto.ReservationRequest;
import com.school.reservation.dto.ReservationVO;
import com.school.reservation.entity.Device;
import com.school.reservation.entity.Reservation;
import com.school.reservation.exception.BizException;
import com.school.reservation.repository.DeviceRepository;
import com.school.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 预约核心逻辑：前置查询校验 + 唯一索引兜底并发
 */
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final DeviceRepository deviceRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public ReservationVO create(ReservationRequest request) {
        Device device = deviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new BizException(HttpStatus.NOT_FOUND.value(), "器材不存在"));

        // 冲突校验：同一器材同一日期同一时段只能被约走一次
        if (reservationRepository.existsByDeviceIdAndDateAndPeriod(
                device.getId(), request.getDate(), request.getPeriod())) {
            throw conflict(device, request);
        }

        Reservation reservation = new Reservation();
        reservation.setDeviceId(device.getId());
        reservation.setDate(request.getDate());
        reservation.setPeriod(request.getPeriod());
        reservation.setUserName(request.getUser().trim());

        try {
            reservationRepository.saveAndFlush(reservation);
        } catch (DataIntegrityViolationException e) {
            // 并发兜底：两个请求同时通过前置校验时，由数据库唯一索引拦下
            throw conflict(device, request);
        }
        return ReservationVO.from(reservation, device.getName());
    }

    public long countByUser(String userName) {
        return reservationRepository.countByUserName(userName.trim());
    }

    /** 我的预约历史列表（按提交时间倒序） */
    public List<ReservationVO> listByUser(String userName) {
        Map<Long, String> deviceNames = deviceRepository.findAll().stream()
                .collect(Collectors.toMap(Device::getId, Device::getName));
        return reservationRepository.findByUserNameOrderByCreatedAtDesc(userName.trim()).stream()
                .map(r -> ReservationVO.from(r, deviceNames.getOrDefault(r.getDeviceId(), "未知器材")))
                .toList();
    }

    /** 我的预约历史（分页）：页码/条数非法直接 400，排序口径为预约日期倒序 */
    public PageVO<ReservationVO> pageByUser(String userName, int page, int size) {
        // 入参校验：页码与每页条数必须为不小于 1 的整数，size 另设上限护栏防大页拖库
        if (page < 1 || size < 1) {
            throw new BizException(HttpStatus.BAD_REQUEST.value(), "页码与每页条数必须为不小于 1 的整数");
        }
        if (size > 100) {
            throw new BizException(HttpStatus.BAD_REQUEST.value(), "每页最多 100 条");
        }
        // 前端页码从 1 开始，Spring Data 的 Pageable 从 0 开始，这里做一次换算
        Page<Reservation> pageData = reservationRepository.findByUserNameOrderByDateDescCreatedAtDesc(
                userName.trim(), PageRequest.of(page - 1, size));
        Map<Long, String> deviceNames = deviceRepository.findAll().stream()
                .collect(Collectors.toMap(Device::getId, Device::getName));
        List<ReservationVO> list = pageData.getContent().stream()
                .map(r -> ReservationVO.from(r, deviceNames.getOrDefault(r.getDeviceId(), "未知器材")))
                .toList();
        return new PageVO<>(list, pageData.getTotalElements(), page, size);
    }

    /** 构造 409 冲突异常，原因说明具体到器材 / 日期 / 时段 */
    private BizException conflict(Device device, ReservationRequest request) {
        String reason = "%s在 %s %s 已被预约，请更换时段".formatted(
                device.getName(), request.getDate(), request.getPeriod());
        return new BizException(HttpStatus.CONFLICT.value(), reason);
    }
}
