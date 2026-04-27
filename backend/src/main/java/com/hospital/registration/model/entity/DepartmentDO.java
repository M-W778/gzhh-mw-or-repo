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
 * 科室信息实体
 */
@TableName(value = "t_department")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(value = "dept_name")
    private String name;

    private String description;

    @TableField(value = "icon_url")
    private String iconUrl;

    @TableField(value = "sort_order")
    private Integer sortOrder = 0;

    @TableField(value = "create_time")
    private LocalDateTime createTime;

    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    @TableField(value = "del_flag")
    private Integer delFlag = 0;

}
