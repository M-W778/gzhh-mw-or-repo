package com.hospital.registration.model.dto.req;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminScheduleUpdateReqDTO {

    @NotNull(message = "totalNum is required")
    @Min(value = 1, message = "totalNum must be greater than 0")
    private Integer totalNum;

    @NotBlank(message = "status is required")
    private String status;
}
