package com.hospital.registration.model.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppointmentReqDTO {
    @NotNull(message = "doctorId is required")
    private Long doctorId;

    @NotNull(message = "scheduleId is required")
    private Long scheduleId;

    @NotNull(message = "patientId is required")
    private Long patientId;

    private String remarks;
}
