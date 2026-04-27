package com.hospital.registration.service;

import com.hospital.registration.model.entity.ScheduleDO;
import com.hospital.registration.model.enums.ScheduleEnum;
import com.hospital.registration.model.enums.TimeSlotEnum;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleService {

    List<ScheduleDO> getSchedulesByDoctorAndDateRange(Long doctorId, LocalDate startDate, LocalDate endDate);

    List<ScheduleDO> getFutureSchedulesByDoctor(Long doctorId);

    ScheduleDO getScheduleById(Long id);

    void decreaseAmount(Long scheduleId);

    void increaseAmount(Long scheduleId);

    ScheduleDO createSchedule(Long doctorId, LocalDate scheduleDate, String timeSlot, Integer totalAmount);

    ScheduleDO updateSchedule(Long scheduleId, Integer totalAmount, String status);

    void deleteSchedule(Long scheduleId);
}
