package com.school.reservation.controller;

import com.school.reservation.dto.DeviceVO;
import com.school.reservation.dto.Result;
import com.school.reservation.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 器材查询
 */
@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceRepository deviceRepository;

    /** GET /api/devices：器材卡片列表（预置：投影仪 / 音响 / 无线话筒） */
    @GetMapping
    public Result<List<DeviceVO>> list() {
        List<DeviceVO> devices = deviceRepository.findAll().stream()
                .map(DeviceVO::from)
                .toList();
        return Result.ok(devices);
    }
}
