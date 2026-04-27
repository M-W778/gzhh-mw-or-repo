package com.hospital.registration.model.dto.req;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AdminScheduleCreateReqDTO {

    @NotNull(message = "doctorId is required")
    private Long doctorId;

    @NotNull(message = "workDate is required")
    @FutureOrPresent(message = "workDate cannot be in the past")
    private LocalDate workDate;

    @NotBlank(message = "workPeriod is required")
    @Pattern(regexp = "^(?i)(MORNING|AFTERNOON)$", message = "workPeriod must be MORNING or AFTERNOON")
    private String workPeriod;

    @NotNull(message = "totalNum is required")
    @Min(value = 1, message = "totalNum must be greater than 0")
    private Integer totalNum;
}
