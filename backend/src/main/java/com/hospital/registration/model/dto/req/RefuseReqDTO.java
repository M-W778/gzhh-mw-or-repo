package com.hospital.registration.model.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefuseReqDTO {
    @NotBlank(message = "取消原因不能为空")
    private String reason;
}
