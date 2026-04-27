package com.hospital.registration.mapper;

import com.hospital.registration.model.entity.DoctorDO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DoctorMapper {

    @Select("SELECT * FROM t_doctor WHERE id = #{id} AND del_flag = 0")
    DoctorDO findById(Long id);

    @Select("SELECT * FROM t_doctor WHERE department_id = #{departmentId} AND del_flag = 0")
    List<DoctorDO> findByDepartmentId(Long departmentId);

    @Select("SELECT * FROM t_doctor WHERE name LIKE CONCAT('%', #{keyword}, '%') AND del_flag = 0")
    List<DoctorDO> searchByName(String keyword);

    @Select("SELECT * FROM t_doctor WHERE del_flag = 0")
    List<DoctorDO> findAllEnabled();

    @Select("SELECT * FROM t_doctor WHERE user_id = #{userId} AND del_flag = 0")
    DoctorDO findByUserId(Long userId);

    @Update("UPDATE t_doctor SET user_id = #{userId}, update_time = NOW() " +
            "WHERE id = #{doctorId} AND user_id IS NULL AND del_flag = 0")
    int bindUser(@Param("doctorId") Long doctorId, @Param("userId") Long userId);

    @Insert("INSERT INTO t_doctor (department_id, user_id, name, title, specialty, introduction, avatar_url, registration_fee, del_flag, create_time, update_time) " +
            "VALUES (#{departmentId}, #{userId}, #{name}, #{title}, #{specialty}, #{introduction}, #{avatarUrl}, #{registrationFee}, 0, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DoctorDO doctorDO);

    @Update("UPDATE t_doctor SET department_id = #{departmentId}, user_id = #{userId}, name = #{name}, title = #{title}, " +
            "specialty = #{specialty}, introduction = #{introduction}, avatar_url = #{avatarUrl}, " +
            "registration_fee = #{registrationFee},  update_time = NOW() WHERE id = #{id}")
    int update(DoctorDO doctorDO);

    @Delete("DELETE FROM t_doctor WHERE id = #{id}")
    int deleteById(Long id);
}
