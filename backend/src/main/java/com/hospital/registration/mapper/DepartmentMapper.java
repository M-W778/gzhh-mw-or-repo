package com.hospital.registration.mapper;

import com.hospital.registration.model.entity.DepartmentDO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DepartmentMapper {

    @Select("""
            SELECT id,
                   dept_name AS name,
                   description,
                   icon_url AS icon_url,
                   sort_order AS sort_order,
                   create_time AS create_time,
                   update_time AS update_time,
                   del_flag AS del_flag
            FROM t_department
            WHERE id = #{id}
              AND del_flag = 0
            """)
    DepartmentDO findById(Long id);

    @Select("""
            SELECT id,
                   dept_name AS name,
                   description,
                   icon_url AS icon_url,
                   sort_order AS sort_order,
                   create_time AS create_time,
                   update_time AS update_time,
                   del_flag AS del_flag
            FROM t_department
            WHERE del_flag = 0
            ORDER BY sort_order
            """)
    List<DepartmentDO> findAllEnabled();

    @Select("""
            SELECT id,
                   dept_name AS name,
                   description,
                   icon_url AS icon_url,
                   sort_order AS sort_order,
                   create_time AS create_time,
                   update_time AS update_time,
                   del_flag AS del_flag
            FROM t_department
            WHERE t_department.dept_name LIKE CONCAT('%', #{deptName}, '%')
              AND del_flag = 0
            ORDER BY sort_order
            """)
    List<DepartmentDO> searchByName(String deptName);

    @Insert("INSERT INTO t_department (dept_name, description, icon_url, sort_order, del_flag, create_time, update_time) " +
            "VALUES (#{name}, #{description}, #{iconUrl}, #{sortOrder}, #{delFlag}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DepartmentDO departmentDO);

    @Update("UPDATE t_department SET dept_name = #{name}, description = #{description}, icon_url = #{iconUrl}, " +
            "sort_order = #{sortOrder}, del_flag = #{delFlag}, update_time = NOW() WHERE id = #{id}")
    int update(DepartmentDO departmentDO);

    @Delete("DELETE FROM t_department WHERE id = #{id}")
    int deleteById(Long id);
}
