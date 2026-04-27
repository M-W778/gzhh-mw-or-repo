package com.hospital.registration.service.impl;

import com.hospital.registration.common.ErrorCode;
import com.hospital.registration.exception.BusinessException;
import com.hospital.registration.mapper.DoctorMapper;
import com.hospital.registration.mapper.ScheduleMapper;
import com.hospital.registration.model.entity.DoctorDO;
import com.hospital.registration.model.entity.ScheduleDO;
import com.hospital.registration.model.enums.ScheduleEnum;
import com.hospital.registration.model.enums.TimeSlotEnum;
import com.hospital.registration.service.ScheduleService;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Resource
    private ScheduleMapper scheduleMapper;

    @Resource
    private DoctorMapper doctorMapper;

    @Override
    public List<ScheduleDO> getSchedulesByDoctorAndDateRange(Long doctorId, LocalDate startDate, LocalDate endDate) {
        if (doctorId == null || doctorId <= 0 || startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "invalid schedule query params");
        }
        return scheduleMapper.findByDoctorIdAndDateRange(doctorId, startDate, endDate);
    }

    @Override
    public List<ScheduleDO> getFutureSchedulesByDoctor(Long doctorId) {
        if (doctorId == null || doctorId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "doctorId is required");
        }
        return scheduleMapper.findFutureByDoctorId(doctorId, LocalDate.now());
    }

    @Override
    public ScheduleDO getScheduleById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "scheduleId is required");
        }
        ScheduleDO scheduleDO = scheduleMapper.findById(id);
        if (scheduleDO == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "schedule not found");
        }
        return scheduleDO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decreaseAmount(Long scheduleId) {
        if (scheduleId == null || scheduleId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "scheduleId is required");
        }

        int updated = scheduleMapper.decreaseQuota(scheduleId);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "no remaining quota");
        }

        ScheduleDO scheduleDO = scheduleMapper.findById(scheduleId);
        if (scheduleDO != null && scheduleDO.getRemainingAmount() != null && scheduleDO.getRemainingAmount() <= 0) {
            scheduleDO.setStatus(ScheduleEnum.FULL.getValue());
            scheduleMapper.update(scheduleDO);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void increaseAmount(Long scheduleId) {
        if (scheduleId == null || scheduleId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "scheduleId is required");
        }
        scheduleMapper.increaseQuota(scheduleId);

        ScheduleDO scheduleDO = scheduleMapper.findById(scheduleId);
        if (scheduleDO != null && Objects.equals(scheduleDO.getStatus(), ScheduleEnum.FULL.getValue())
                && scheduleDO.getRemainingAmount() != null && scheduleDO.getRemainingAmount() > 0) {
            scheduleDO.setStatus(ScheduleEnum.AVAILABLE.getValue());
            scheduleMapper.update(scheduleDO);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ScheduleDO createSchedule(Long doctorId, LocalDate scheduleDate, String timeSlot, Integer totalAmount) {
        validateCreateParams(doctorId, scheduleDate, timeSlot, totalAmount);

        DoctorDO doctorDO = doctorMapper.findById(doctorId);
        if (doctorDO == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "doctor not found");
        }

        ScheduleDO existing = scheduleMapper.findByDoctorAndDateAndSlot(doctorId, scheduleDate, timeSlot);
        if (existing != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "duplicate schedule on same date and period");
        }

        ScheduleDO scheduleDO = new ScheduleDO();
        scheduleDO.setDoctorId(doctorId);
        scheduleDO.setScheduleDate(scheduleDate);
        scheduleDO.setTimeSlot(timeSlot);
        scheduleDO.setTotalAmount(totalAmount);
        scheduleDO.setRemainingAmount(totalAmount);
        scheduleDO.setStatus(ScheduleEnum.AVAILABLE.getValue());

        try {
            scheduleMapper.insert(scheduleDO);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "duplicate schedule on same date and period");
        }
        return scheduleDO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ScheduleDO updateSchedule(Long scheduleId, Integer totalAmount, String status) {
        if (scheduleId == null || scheduleId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "scheduleId is required");
        }
        if (totalAmount == null || totalAmount <= 0 || status == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "invalid update params");
        }

        ScheduleDO scheduleDO = scheduleMapper.findById(scheduleId);
        if (scheduleDO == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "schedule not found");
        }

        int usedAmount = scheduleDO.getTotalAmount() - scheduleDO.getRemainingAmount();
        if (totalAmount < usedAmount) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "total quota cannot be lower than used quota");
        }

        scheduleDO.setTotalAmount(totalAmount);
        scheduleDO.setRemainingAmount(totalAmount - usedAmount);
        scheduleDO.setStatus(status);
        scheduleMapper.update(scheduleDO);
        return scheduleDO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSchedule(Long scheduleId) {
        if (scheduleId == null || scheduleId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "scheduleId is required");
        }
        ScheduleDO scheduleDO = scheduleMapper.findById(scheduleId);
        if (scheduleDO == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "schedule not found");
        }

        int usedQuota = scheduleDO.getTotalAmount() - scheduleDO.getRemainingAmount();
        if (usedQuota > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "schedule already has appointments");
        }
        scheduleMapper.deleteById(scheduleId);
    }

    private void validateCreateParams(Long doctorId, LocalDate scheduleDate, String timeSlot, Integer totalAmount) {
        if (doctorId == null || doctorId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "doctorId is required");
        }
        if (scheduleDate == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "workDate is required");
        }
        if (scheduleDate.isBefore(LocalDate.now())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "workDate cannot be in the past");
        }
        if (timeSlot == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "workPeriod is invalid");
        }
        if (totalAmount == null || totalAmount <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "totalNum must be greater than 0");
        }
    }
}
