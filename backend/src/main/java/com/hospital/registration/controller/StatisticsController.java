package com.hospital.registration.controller;

import com.hospital.registration.common.BaseResponse;
import com.hospital.registration.common.ResultUtils;
import com.hospital.registration.mapper.AppointmentMapper;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistic")
public class StatisticsController {

    @Resource
    private AppointmentMapper appointmentMapper;

    /**
     * 查询预约统计信息（管理员）。
     *
     * @return 预约统计数据
     */
    @GetMapping("/appointments")
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse<Map<String, Object>> getAppointmentStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long todayAppointments = appointmentMapper.countTodayAppointments();
        stats.put("todayAppointments", todayAppointments);

        long todayPending = appointmentMapper.countTodayPendingAppointments();
        stats.put("todayPending", todayPending);

        long totalPending = appointmentMapper.countTotalPendingAppointments();
        stats.put("totalPending", totalPending);

        long totalCompleted = appointmentMapper.countTotalCompletedAppointments();
        stats.put("totalCompleted", totalCompleted);

        long totalCancelled = appointmentMapper.countTotalCancelledAppointments();
        stats.put("totalCancelled", totalCancelled);

        List<AppointmentMapper.DepartmentStat> topDepartments = appointmentMapper.findTopDepartmentsLast7Days();
        stats.put("topDepartments", topDepartments);

        List<AppointmentMapper.DailyStat> dailyStats = appointmentMapper.findDailyAppointmentsLast7Days();
        stats.put("dailyStats", dailyStats);

        return ResultUtils.success(stats);
    }

    /**
     * 查询今日预约统计（公开接口）。
     *
     * @return 今日预约统计数据
     */
    @GetMapping("/today")
    public BaseResponse<Map<String, Long>> getTodayStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("todayAppointments", appointmentMapper.countTodayAppointments());
        stats.put("todayPending", appointmentMapper.countTodayPendingAppointments());
        return ResultUtils.success(stats);
    }
}
