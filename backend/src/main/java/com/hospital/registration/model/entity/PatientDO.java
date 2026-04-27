package com.hospital.registration.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 患者信息实体
 */
@TableName(value = "t_patient")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(value = "user_id")
    private Long userId;

    @TableField(value = "real_name")
    private String realName;

    @TableField(value = "id_card")
    private String idCard;

    private String phone;

    /**
     * 0=female, 1=male
     */
    private Integer gender;

    @TableField(value = "birth_date")
    private LocalDate birthDate;

    @TableField(value = "is_default")
    private Boolean isDefault = false;

    @TableField(value = "create_time")
    private LocalDateTime createTime;

    @TableField(value = "update_time")
    private LocalDateTime updateTime;
}
