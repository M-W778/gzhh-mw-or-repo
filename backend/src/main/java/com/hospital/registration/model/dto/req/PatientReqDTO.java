package com.hospital.registration.model.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientReqDTO {

    private Long id;

    @NotBlank(message = "请输入姓名")
    private String realName;

    @NotBlank(message = "身份证号码不能为空")
    private String idCard;

    @NotBlank(message = "手机号码不能为空")
    private String phone;

    /**
     * 0=female, 1=male
     */
    @NotNull(message = "性别不能为空")
    private Integer gender;

    private LocalDate birthDate;

    private Boolean isDefault = false;
}
