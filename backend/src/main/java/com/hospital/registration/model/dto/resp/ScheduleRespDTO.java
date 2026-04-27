package com.hospital.registration.model.dto.resp;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ScheduleRespDTO {
    private Long id;
    private Long doctorId;
    private LocalDate workDate;
    private String workPeriod;
    private String workPeriodText;
    private Integer totalNum;
    private Integer remainingNum;
    private String status;
    private String statusText;
}
