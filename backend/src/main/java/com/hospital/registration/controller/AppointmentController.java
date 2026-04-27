package com.hospital.registration.controller;

import com.hospital.registration.common.BaseResponse;
import com.hospital.registration.common.ErrorCode;
import com.hospital.registration.common.ResultUtils;
import com.hospital.registration.config.UserDetailsImpl;
import com.hospital.registration.exception.BusinessException;
import com.hospital.registration.model.dto.req.AppointmentReqDTO;
import com.hospital.registration.model.dto.req.RefuseReqDTO;
import com.hospital.registration.model.entity.AppointmentDO;
import com.hospital.registration.model.entity.DepartmentDO;
import com.hospital.registration.model.entity.DoctorDO;
import com.hospital.registration.model.entity.PatientDO;
import com.hospital.registration.model.enums.AppointmentStatusEnum;
import com.hospital.registration.model.enums.TimeSlotEnum;
import com.hospital.registration.service.AppointmentService;
import com.hospital.registration.service.DepartmentService;
import com.hospital.registration.service.DoctorService;
import com.hospital.registration.service.PatientService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/appointment")
public class AppointmentController {

    @Resource
    private AppointmentService appointmentService;

    @Resource
    private PatientService patientService;

    @Resource
    private DoctorService doctorService;

    @Resource
    private DepartmentService departmentService;

    /**
     * 查询当前登录用户的预约列表。
     *
     * @param userDetails 当前登录用户信息
     * @return 预约列表
     */
    @GetMapping
    public BaseResponse<List<Map<String, Object>>> getMyAppointments(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = getLoginUserId(userDetails);
        List<AppointmentDO> appointments = appointmentService.getAppointmentsByUserId(userId);
        List<Map<String, Object>> result = appointments.stream().map(this::convertToMap).collect(Collectors.toList());
        return ResultUtils.success(result);
    }

    /**
     * 根据预约 ID 查询预约详情。
     *
     * @param id 预约 ID
     * @param userDetails 当前登录用户信息
     * @return 预约详情
     */
    @GetMapping("/{id}")
    public BaseResponse<Map<String, Object>> getAppointmentById(@PathVariable Long id,
                                                                @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = getLoginUserId(userDetails);
        AppointmentDO appointmentDO = appointmentService.getAppointmentById(id);
        if (!appointmentDO.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "no permission");
        }
        return ResultUtils.success(convertToMap(appointmentDO));
    }

    /**
     * 提交预约申请并返回预约成功信息。
     *
     * @param request 预约请求参数
     * @param userDetails 当前登录用户信息
     * @return 预约结果
     */
    @PostMapping
    public BaseResponse<Map<String, Object>> createAppointment(@Valid @RequestBody AppointmentReqDTO request,
                                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = getLoginUserId(userDetails);
        AppointmentDO appointmentDO = appointmentService.createAppointment(userId, request);
        Map<String, Object> data = convertToMap(appointmentDO);
        data.put("successNotice", "appointment created successfully");
        return ResultUtils.success(data);
    }

    /**
     * 取消预约。
     *
     * @param id 预约 ID
     * @param request 取消原因请求参数
     * @param userDetails 当前登录用户信息
     * @return 取消后的预约信息
     */
    @PostMapping("/{id}/cancel")
    public BaseResponse<Map<String, Object>> cancelAppointment(@PathVariable Long id,
                                                               @Valid @RequestBody RefuseReqDTO request,
                                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = getLoginUserId(userDetails);
        AppointmentDO appointmentDO = appointmentService.refuseAppointment(userId, id, request.getReason());
        return ResultUtils.success(convertToMap(appointmentDO));
    }

    /**
     * 判断预约是否可取消。
     *
     * @param id 预约 ID
     * @param userDetails 当前登录用户信息
     * @return 是否可取消及原因
     */
    @GetMapping("/{id}/can-cancel")
    public BaseResponse<Map<String, Object>> canCancelAppointment(@PathVariable Long id,
                                                                  @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = getLoginUserId(userDetails);
        AppointmentDO appointmentDO = appointmentService.getAppointmentById(id);
        if (!appointmentDO.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "no permission");
        }
        boolean canCancel = appointmentService.canCancel(appointmentDO);
        Map<String, Object> result = new HashMap<>();
        result.put("canCancel", canCancel);
        if (!canCancel) {
            result.put("reason", !AppointmentStatusEnum.PENDING.getValue().equalsIgnoreCase(appointmentDO.getStatus())
                    ? "only " + AppointmentStatusEnum.PENDING.getValue() + " appointment can be cancelled"
                    : "cancel window expired");
        }
        return ResultUtils.success(result);
    }

    /**
     * 标记预约通知已发送。
     *
     * @param id 预约 ID
     * @param userDetails 当前登录用户信息
     * @return 是否标记成功
     */
    @PostMapping("/{id}/notify")
    public BaseResponse<Boolean> markNotificationSent(@PathVariable Long id,
                                                      @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = getLoginUserId(userDetails);
        AppointmentDO appointmentDO = appointmentService.getAppointmentById(id);
        if (!appointmentDO.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "no permission");
        }
        appointmentService.markNotificationSent(id);
        return ResultUtils.success(true);
    }

    private Long getLoginUserId(UserDetailsImpl userDetails) {
        if (userDetails == null || userDetails.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "not login");
        }
        return userDetails.getId();
    }

    private Map<String, Object> convertToMap(AppointmentDO appointmentDO) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", appointmentDO.getId());
        map.put("appointmentNo", appointmentDO.getAppointmentNo());

        try {
            PatientDO patientDO = patientService.getPatientById(appointmentDO.getPatientId());
            map.put("patientName", patientDO.getRealName());
            map.put("patientIdCard", patientDO.getIdCard());
        } catch (Exception e) {
            map.put("patientName", "unknown");
            map.put("patientIdCard", "");
        }

        try {
            DoctorDO doctorDO = doctorService.getDoctorById(appointmentDO.getDoctorId());
            map.put("doctorName", doctorDO.getName());
            map.put("doctorTitle", doctorDO.getTitle());

            String departmentName = "";
            if (doctorDO.getDepartmentId() != null) {
                DepartmentDO departmentDO = departmentService.getDepartmentById(doctorDO.getDepartmentId());
                departmentName = departmentDO == null ? "" : departmentDO.getName();
            }
            map.put("departmentName", departmentName);
        } catch (Exception e) {
            map.put("doctorName", "unknown");
            map.put("doctorTitle", "");
            map.put("departmentName", "");
        }

        String timeSlot = appointmentDO.getTimeSlot();
        String status = appointmentDO.getStatus();

        map.put("appointmentDate", appointmentDO.getAppointmentDate());
        map.put("timeSlot", timeSlot == null ? "" : timeSlot);
        map.put("timeSlotText", toTimeSlotText(timeSlot));
        map.put("status", status == null ? "" : status);
        map.put("statusText", toStatusText(status));
        map.put("queueNumber", appointmentDO.getQueueNumber());
        map.put("remarks", appointmentDO.getRemarks());
        map.put("createdAt", appointmentDO.getCreateTime());
        map.put("cancelledAt", appointmentDO.getRefuseTime());
        map.put("cancelReason", appointmentDO.getRefuseReason());
        map.put("canCancel", appointmentService.canCancel(appointmentDO));
        map.put("notificationSent", appointmentDO.getNotificationSent());
        map.put("notices", buildNotices());
        return map;
    }

    private List<String> buildNotices() {
        return List.of(
                "Please arrive 15 minutes before your appointment time.",
                "Bring your ID card and related medical records.",
                "If you need to cancel, please do it within 30 minutes after booking."
        );
    }

    private String toTimeSlotText(String timeSlot) {
        TimeSlotEnum slotEnum = TimeSlotEnum.fromValue(timeSlot);
        return slotEnum == null ? timeSlot : slotEnum.getText();
    }

    private String toStatusText(String status) {
        AppointmentStatusEnum statusEnum = AppointmentStatusEnum.getEnumByValue(status);
        return statusEnum == null ? status : statusEnum.getText();
    }
}
