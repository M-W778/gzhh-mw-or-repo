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
 * 预约信息实体
 */
@TableName(value = "t_appointment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(value = "appointment_no")
    private String appointmentNo;

    @TableField(value = "user_id")
    private Long userId;

    @TableField(value = "patient_id")
    private Long patientId;

    @TableField(value = "doctor_id")
    private Long doctorId;

    @TableField(value = "department_id")
    private Long departmentId;

    @TableField(value = "schedule_id")
    private Long scheduleId;

    @TableField(value = "appointment_date")
    private LocalDate appointmentDate;

    @TableField(value = "time_slot")
    private String timeSlot;

    private String status;

    @TableField(value = "queue_number")
    private Integer queueNumber;

    private String remarks;

    @TableField(value = "refuse_time")
    private LocalDateTime refuseTime;

    @TableField(value = "refuse_reason")
    private String refuseReason;

    @TableField(value = "notification_sent")
    private Boolean notificationSent = false;

    @TableField(value = "create_time")
    private LocalDateTime createTime;

    @TableField(value = "update_time")
    private LocalDateTime updatedTime;
}
