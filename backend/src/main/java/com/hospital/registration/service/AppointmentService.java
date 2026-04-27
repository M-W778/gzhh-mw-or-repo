package com.hospital.registration.service;

import com.hospital.registration.model.dto.req.AppointmentReqDTO;
import com.hospital.registration.model.entity.AppointmentDO;

import java.util.List;

public interface AppointmentService {
    List<AppointmentDO> getAppointmentsByUserId(Long userId);

    AppointmentDO getAppointmentById(Long id);

    AppointmentDO createAppointment(Long userId, AppointmentReqDTO request);

    void markNotificationSent(Long appointmentId);

    AppointmentDO refuseAppointment(Long userId, Long appointmentId, String reason);

    boolean canCancel(AppointmentDO appointmentDO);

}
