package com.hospital.registration.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户信息实体
 */
@TableName(value = "t_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String phone;

    private String email;


    @TableField(value = "user_type")
    private String role;

    /**
     * deletion_time（0 表示未注销）
     */
    @TableField(value = "deletion_time")
    private Long deletionTime = 0L;


    @TableField(value = "create_time")
    private LocalDateTime createTime;


    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    /**
     * del_flag（0 未删，1 已删）
     */
    @TableField(value = "del_flag")
    private Integer delFlag = 0;

}
