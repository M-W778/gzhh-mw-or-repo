package com.hospital.registration.controller;

import com.hospital.registration.common.BaseResponse;
import com.hospital.registration.common.ErrorCode;
import com.hospital.registration.common.ResultUtils;
import com.hospital.registration.config.UserDetailsImpl;
import com.hospital.registration.exception.BusinessException;
import com.hospital.registration.mapper.AppointmentMapper;
import com.hospital.registration.mapper.DoctorMapper;
import com.hospital.registration.mapper.PatientMapper;
import com.hospital.registration.model.entity.AppointmentDO;
import com.hospital.registration.model.entity.DoctorDO;
import com.hospital.registration.model.entity.PatientDO;
import com.hospital.registration.model.enums.AppointmentStatusEnum;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctor")
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorAppointmentController {

    @Resource
    private DoctorMapper doctorMapper;

    @Resource
    private AppointmentMapper appointmentMapper;

    @Resource
    private PatientMapper patientMapper;

    /**
     * 查询当前登录医生的个人信息。
     *
     * @param userDetails 当前登录医生信息
     * @return 医生详情
     */
    @GetMapping("/profile")
    public BaseResponse<DoctorDO> getDoctorProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        DoctorDO doctorDO = doctorMapper.findByUserId(userDetails.getId());
        if (doctorDO == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "doctor profile not found");
        }
        return ResultUtils.success(doctorDO);
    }

    /**
     * 查询当前登录医生的全部预约记录。
     *
     * @param userDetails 当前登录医生信息
     * @return 预约列表
     */
    @GetMapping("/appointments")
    public BaseResponse<List<Map<String, Object>>> getDoctorAppointments(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        DoctorDO doctorDO = doctorMapper.findByUserId(userDetails.getId());
        if (doctorDO == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "doctor profile not found");
        }
        List<AppointmentDO> appointments = appointmentMapper.findByDoctorId(doctorDO.getId());
        return ResultUtils.success(appointments.stream().map(this::toDoctorAppointmentResp).toList());
    }

    /**
     * 查询当前登录医生今日预约记录。
     *
     * @param userDetails 当前登录医生信息
     * @return 今日预约列表
     */
    @GetMapping("/appointments/today")
    public BaseResponse<List<Map<String, Object>>> getTodayAppointments(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        DoctorDO doctorDO = doctorMapper.findByUserId(userDetails.getId());
        if (doctorDO == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "doctor profile not found");
        }
        List<AppointmentDO> appointments = appointmentMapper.findByDoctorIdAndDate(doctorDO.getId(), LocalDate.now());
        return ResultUtils.success(appointments.stream().map(this::toDoctorAppointmentResp).toList());
    }

    /**
     * 按状态查询当前登录医生的预约记录。
     *
     * @param userDetails 当前登录医生信息
     * @param status 预约状态
     * @return 预约列表
     */
    @GetMapping("/appointments/status/{status}")
    public BaseResponse<List<Map<String, Object>>> getAppointmentsByStatus(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                           @PathVariable String status) {
        DoctorDO doctorDO = doctorMapper.findByUserId(userDetails.getId());
        if (doctorDO == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "doctor profile not found");
        }
        List<AppointmentDO> appointments = appointmentMapper.findByDoctorIdAndStatus(doctorDO.getId(), status.toUpperCase());
        return ResultUtils.success(appointments.stream().map(this::toDoctorAppointmentResp).toList());
    }

    /**
     * 查询当前登录医生的预约统计信息。
     *
     * @param userDetails 当前登录医生信息
     * @return 统计结果
     */
    @GetMapping("/statistics")
    public BaseResponse<Map<String, Object>> getDoctorStatistics(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        DoctorDO doctorDO = doctorMapper.findByUserId(userDetails.getId());
        if (doctorDO == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "doctor profile not found");
        }

        List<AppointmentDO> allAppointments = appointmentMapper.findByDoctorId(doctorDO.getId());
        long total = allAppointments.size();
        long pending = allAppointments.stream().filter(a -> AppointmentStatusEnum.PENDING.getValue().equalsIgnoreCase(a.getStatus())).count();
        long completed = allAppointments.stream().filter(a -> AppointmentStatusEnum.COMPLETED.getValue().equalsIgnoreCase(a.getStatus())).count();
        long cancelled = allAppointments.stream().filter(a -> AppointmentStatusEnum.CANCELLED.getValue().equalsIgnoreCase(a.getStatus())).count();
        long todayCount = allAppointments.stream().filter(a -> LocalDate.now().equals(a.getAppointmentDate())).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("completed", completed);
        stats.put("cancelled", cancelled);
        stats.put("todayCount", todayCount);
        return ResultUtils.success(stats);
    }

    /**
     * 将指定预约标记为已完成。
     *
     * @param userDetails 当前登录医生信息
     * @param id 预约 ID
     * @return 操作结果
     */
    @PostMapping("/appointments/{id}/complete")
    public BaseResponse<String> completeAppointment(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                    @PathVariable Long id) {
        DoctorDO doctorDO = doctorMapper.findByUserId(userDetails.getId());
        if (doctorDO == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "doctor profile not found");
        }

        AppointmentDO appointmentDO = appointmentMapper.findById(id);
        if (appointmentDO == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "appointment not found");
        }
        if (!appointmentDO.getDoctorId().equals(doctorDO.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "no permission");
        }
        if (!AppointmentStatusEnum.PENDING.getValue().equalsIgnoreCase(appointmentDO.getStatus())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "only pending appointment can be completed");
        }

        appointmentDO.setStatus(AppointmentStatusEnum.COMPLETED.getValue());
        appointmentMapper.updateStatus(appointmentDO);
        return ResultUtils.success("completed");
    }

    private Map<String, Object> toDoctorAppointmentResp(AppointmentDO appointmentDO) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", appointmentDO.getId());
        map.put("appointmentNo", appointmentDO.getAppointmentNo());
        map.put("patientId", appointmentDO.getPatientId());
        map.put("doctorId", appointmentDO.getDoctorId());
        map.put("departmentId", appointmentDO.getDepartmentId());
        map.put("scheduleId", appointmentDO.getScheduleId());
        map.put("appointmentDate", appointmentDO.getAppointmentDate());
        map.put("timeSlot", appointmentDO.getTimeSlot());
        map.put("status", appointmentDO.getStatus());
        map.put("queueNumber", appointmentDO.getQueueNumber());
        map.put("remarks", appointmentDO.getRemarks());
        map.put("createdAt", appointmentDO.getCreateTime());
        map.put("updatedAt", appointmentDO.getUpdatedTime());

        PatientDO patientDO = appointmentDO.getPatientId() == null ? null : patientMapper.findById(appointmentDO.getPatientId());
        map.put("patientName", patientDO == null ? "unknown" : patientDO.getRealName());
        map.put("patientIdCard", patientDO == null ? "" : patientDO.getIdCard());
        map.put("patientPhone", patientDO == null ? "" : patientDO.getPhone());
        return map;
    }
}
