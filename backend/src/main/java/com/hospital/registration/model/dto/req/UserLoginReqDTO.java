package com.hospital.registration.model.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginReqDTO {
    /**
     * phone or email (also compatible with username)
     */
    @NotBlank(message = "account is required")
    private String username;

    @NotBlank(message = "password is required")
    private String password;
}
