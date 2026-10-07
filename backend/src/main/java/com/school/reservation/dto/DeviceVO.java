package com.school.reservation.dto;

import com.school.reservation.entity.Device;

/**
 * 器材卡片展示对象
 */
public record DeviceVO(Long id, String name, String description) {

    public static DeviceVO from(Device device) {
        return new DeviceVO(device.getId(), device.getName(), device.getDescription());
    }
}
