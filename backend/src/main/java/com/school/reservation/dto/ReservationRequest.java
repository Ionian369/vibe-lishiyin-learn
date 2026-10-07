package com.school.reservation.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 提交预约请求体
 */
@Getter
@Setter
public class ReservationRequest {

    @NotNull(message = "请选择器材")
    private Long deviceId;

    @NotNull(message = "请选择日期")
    @FutureOrPresent(message = "预约日期不能早于今天")
    private LocalDate date;

    @NotBlank(message = "请选择时段")
    @Pattern(regexp = "上午|下午", message = "时段只能是上午或下午")
    private String period;

    @NotBlank(message = "请填写姓名")
    @Size(max = 50, message = "姓名不能超过 50 字")
    private String user;
}
