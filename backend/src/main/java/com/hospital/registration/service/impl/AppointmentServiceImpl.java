package com.hospital.registration.service.impl;

import com.hospital.registration.common.ErrorCode;
import com.hospital.registration.exception.BusinessException;
import com.hospital.registration.mapper.AppointmentMapper;
import com.hospital.registration.mapper.ScheduleMapper;
import com.hospital.registration.model.dto.req.AppointmentReqDTO;
import com.hospital.registration.model.entity.AppointmentDO;
import com.hospital.registration.model.entity.DoctorDO;
import com.hospital.registration.model.entity.PatientDO;
import com.hospital.registration.model.entity.ScheduleDO;
import com.hospital.registration.model.entity.UserDO;
import com.hospital.registration.model.enums.AppointmentStatusEnum;
import com.hospital.registration.model.enums.ScheduleEnum;
import com.hospital.registration.service.AppointmentService;
import com.hospital.registration.service.DoctorService;
import com.hospital.registration.service.PatientService;
import com.hospital.registration.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    @Resource
    private AppointmentMapper appointmentMapper;

    @Resource
    private UserService userService;

    @Resource
    private PatientService patientService;

    @Resource
    private DoctorService doctorService;

    @Resource
    private ScheduleMapper scheduleMapper;

    @Override
    public List<AppointmentDO> getAppointmentsByUserId(Long userId) {
        return appointmentMapper.findByUserId(userId);
    }

    @Override
    public AppointmentDO getAppointmentById(Long id) {
        AppointmentDO appointmentDO = appointmentMapper.findById(id);
        if (appointmentDO == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "appointment not found");
        }
        return appointmentDO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppointmentDO createAppointment(Long userId, AppointmentReqDTO request) {
        if (userId == null || userId <= 0 || request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "invalid request");
        }
        if (request.getPatientId() == null || request.getDoctorId() == null || request.getScheduleId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "patientId, doctorId and scheduleId are required");
        }

        UserDO userDO = userService.findById(userId);
        if (userDO == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "user not found");
        }

        PatientDO patientDO = patientService.getPatientById(request.getPatientId());
        if (patientDO == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "patient not found");
        }
        if (!userId.equals(patientDO.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "patient does not belong to current user");
        }

        DoctorDO doctorDO = doctorService.getDoctorById(request.getDoctorId());
        if (doctorDO == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "doctor not found");
        }
        if (doctorDO.getDepartmentId() == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "doctor department is missing");
        }

        ScheduleDO scheduleDO = scheduleMapper.findById(request.getScheduleId());
        if (scheduleDO == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "schedule not found");
        }
        if (!scheduleDO.getDoctorId().equals(doctorDO.getId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "schedule does not match doctor");
        }
        if (scheduleDO.getScheduleDate() == null || scheduleDO.getScheduleDate().isBefore(LocalDate.now())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "cannot make appointment for past date");
        }
        if (Objects.equals(scheduleDO.getStatus(), ScheduleEnum.SUSPENDED.getValue())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "schedule is suspended");
        }
        if (Objects.equals(scheduleDO.getStatus(), ScheduleEnum.FULL.getValue())
                || scheduleDO.getRemainingAmount() == null
                || scheduleDO.getRemainingAmount() <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "no remaining quota");
        }

        long existingCount = appointmentMapper.countActiveByPatientAndDepartmentAndDate(
                patientDO.getId(),
                doctorDO.getDepartmentId(),
                scheduleDO.getScheduleDate());
        if (existingCount > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "the patient already has an appointment in this department on that date");
        }

        int updated = scheduleMapper.decreaseQuota(scheduleDO.getId());
        if (updated == 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "no remaining quota");
        }

        ScheduleDO currentSchedule = scheduleMapper.findById(scheduleDO.getId());
        if (currentSchedule != null && currentSchedule.getRemainingAmount() != null && currentSchedule.getRemainingAmount() <= 0) {
            currentSchedule.setStatus(ScheduleEnum.FULL.getValue());
            scheduleMapper.update(currentSchedule);
        }

        AppointmentDO appointmentDO = new AppointmentDO();
        appointmentDO.setAppointmentNo(generateUniqueAppointmentNo());
        appointmentDO.setUserId(userDO.getId());
        appointmentDO.setPatientId(patientDO.getId());
        appointmentDO.setDoctorId(doctorDO.getId());
        appointmentDO.setDepartmentId(doctorDO.getDepartmentId());
        appointmentDO.setScheduleId(scheduleDO.getId());
        appointmentDO.setAppointmentDate(scheduleDO.getScheduleDate());
        appointmentDO.setTimeSlot(scheduleDO.getTimeSlot() == null ? null : scheduleDO.getTimeSlot());
        appointmentDO.setStatus(AppointmentStatusEnum.PENDING.getValue());
        appointmentDO.setQueueNumber((int) appointmentMapper.countActiveByScheduleId(scheduleDO.getId()) + 1);
        appointmentDO.setRemarks(request.getRemarks());
        appointmentDO.setNotificationSent(false);

        try {
            appointmentMapper.insert(appointmentDO);
        } catch (DuplicateKeyException e) {
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("uk_patient_dept_date")) {
                throw new BusinessException(
                        ErrorCode.OPERATION_ERROR,
                        "appointment conflict on same patient/department/date. " +
                                "If this comes from a cancelled record, please run DB migration to drop uk_patient_dept_date."
                );
            }
            if (errorMessage != null && errorMessage.contains("uk_appointment_no")) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "failed to generate unique appointment number");
            }
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "create appointment failed: duplicate key");
        }
        return appointmentDO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markNotificationSent(Long appointmentId) {
        AppointmentDO appointmentDO = appointmentMapper.findById(appointmentId);
        if (appointmentDO == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "appointment not found");
        }
        appointmentMapper.markNotificationSent(appointmentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppointmentDO refuseAppointment(Long userId, Long appointmentId, String reason) {
        AppointmentDO appointmentDO = appointmentMapper.findById(appointmentId);
        if (appointmentDO == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "appointment not found");
        }
        if (!appointmentDO.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "no permission");
        }
        if (!AppointmentStatusEnum.PENDING.getValue().equalsIgnoreCase(appointmentDO.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "only pending appointments can be cancelled");
        }
        LocalDateTime createTime = appointmentDO.getCreateTime();
        if (createTime != null && createTime.plusMinutes(30).isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "cancel window expired");
        }

        scheduleMapper.increaseQuota(appointmentDO.getScheduleId());
        ScheduleDO scheduleDO = scheduleMapper.findById(appointmentDO.getScheduleId());
        if (scheduleDO != null
                && Objects.equals(scheduleDO.getStatus(), ScheduleEnum.FULL.getValue())
                && scheduleDO.getRemainingAmount() != null
                && scheduleDO.getRemainingAmount() > 0) {
            scheduleDO.setStatus(ScheduleEnum.AVAILABLE.getValue());
            scheduleMapper.update(scheduleDO);
        }

        appointmentDO.setStatus(AppointmentStatusEnum.CANCELLED.getValue());
        appointmentDO.setRefuseTime(LocalDateTime.now());
        appointmentDO.setRefuseReason(reason);
        appointmentMapper.updateStatus(appointmentDO);
        return appointmentDO;
    }

    @Override
    public boolean canCancel(AppointmentDO appointmentDO) {
        if (!AppointmentStatusEnum.PENDING.getValue().equalsIgnoreCase(appointmentDO.getStatus())) {
            return false;
        }
        LocalDateTime createTime = appointmentDO.getCreateTime();
        if (createTime == null) {
            return true;
        }
        return !createTime.plusMinutes(30).isBefore(LocalDateTime.now());
    }

    private String generateUniqueAppointmentNo() {
        for (int i = 0; i < 3; i++) {
            String appointmentNo = generateAppointmentNo();
            if (appointmentMapper.findByAppointmentNo(appointmentNo) == null) {
                return appointmentNo;
            }
        }
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "failed to generate unique appointment number");
    }

    private String generateAppointmentNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "AP" + dateStr + random;
    }
}
