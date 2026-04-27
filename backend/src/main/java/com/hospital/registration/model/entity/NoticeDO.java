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

@TableName(value = "t_notice")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoticeDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String tag;

    @TableField(value = "tag_type")
    private String tagType;

    private String title;

    private String summary;

    @TableField(value = "publish_date")
    private LocalDate publishDate;

    private Boolean pinned = false;

    private String path;

    private Boolean enabled = true;

    @TableField(value = "del_flag")
    private Integer delFlag = 0;

    @TableField(value = "create_time")
    private LocalDateTime createTime;

    @TableField(value = "update_time")
    private LocalDateTime updateTime;
}
