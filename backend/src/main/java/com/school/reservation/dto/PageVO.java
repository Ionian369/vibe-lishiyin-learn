package com.school.reservation.dto;

import java.util.List;

/**
 * 通用分页返回结构
 *
 * @param list  当前页数据
 * @param total 总条数
 * @param page  页码（从 1 开始）
 * @param size  每页条数
 */
public record PageVO<T>(
        List<T> list,
        long total,
        int page,
        int size) {
}
