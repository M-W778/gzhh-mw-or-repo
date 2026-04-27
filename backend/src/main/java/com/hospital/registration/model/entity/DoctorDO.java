package com.hospital.registration.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 医生信息实体
 */
@TableName(value = "t_doctor")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(value = "department_id")
    private Long departmentId;

    @TableField(value = "user_id")
    private Long userId;

    private String name;

    private String title;

    private String specialty;

    private String introduction;

    @TableField(value = "avatar_url")
    private String avatarUrl;

    @TableField(value = "registration_fee")
    private BigDecimal registrationFee = BigDecimal.ZERO;

    @TableField(value = "del_flag")
    private Integer delFlag = 0;

    @TableField(value = "create_time")
    private LocalDateTime createTime;

    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private DepartmentDO departmentDO;

    @TableField(exist = false)
    private UserDO userDO;
}
