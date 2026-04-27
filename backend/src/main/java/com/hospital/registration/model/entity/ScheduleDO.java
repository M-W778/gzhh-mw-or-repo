package com.hospital.registration.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.registration.model.enums.ScheduleEnum;
import com.hospital.registration.model.enums.TimeSlotEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 排班信息实体
 */
@TableName(value = "t_schedule")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(value = "doctor_id")
    private Long doctorId;

    @TableField(value = "schedule_date")
    private LocalDate scheduleDate;

    @TableField(value = "time_slot")
    private String timeSlot;

    @TableField(value = "total_amount")
    private Integer totalAmount;

    @TableField(value = "remaining_amount")
    private Integer remainingAmount;

    private String status;

    private Long version = 0L;

    @TableField(value = "create_time")
    private LocalDateTime createTime;

    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private DoctorDO doctorDO;

    public Long getDoctorId() {
        if (doctorId != null) {
            return doctorId;
        }
        return doctorDO != null ? doctorDO.getId() : null;
    }
}
