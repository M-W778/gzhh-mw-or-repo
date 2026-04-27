package com.hospital.registration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.registration.model.entity.PatientDO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PatientMapper extends BaseMapper<PatientDO> {

    @Select("SELECT * FROM t_patient WHERE id = #{id}")
    PatientDO findById(Long id);

    @Select("SELECT * FROM t_patient WHERE user_id = #{userId}")
    List<PatientDO> queryByUserId(Long userId);

    @Select("SELECT * FROM t_patient WHERE user_id = #{userId} AND id_card = #{idCard}")
    PatientDO findByUserIdAndIdCard(@Param("userId") Long userId, @Param("idCard") String idCard);

    @Select("SELECT COUNT(*) FROM t_patient WHERE user_id = #{userId} AND id_card = #{idCard}")
    boolean existsByUserIdAndIdCard(@Param("userId") Long userId, @Param("idCard") String idCard);

    @Select("SELECT * FROM t_patient WHERE user_id = #{userId} AND is_default = true")
    PatientDO findDefaultByUserId(Long userId);

    @Select("SELECT COUNT(*) FROM t_patient WHERE user_id = #{userId}")
    long countByUserId(Long userId);

    @Insert("INSERT INTO t_patient (user_id, real_name, id_card, phone, gender, birth_date, is_default, create_time, update_time) " +
            "VALUES (#{userId}, #{realName}, #{idCard}, #{phone}, #{gender}, #{birthDate}, #{isDefault}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PatientDO patientDO);

    @Update("UPDATE t_patient SET real_name = #{realName}, id_card = #{idCard}, phone = #{phone}, " +
            "gender = #{gender}, birth_date = #{birthDate}, is_default = #{isDefault}, update_time = #{updateTime} WHERE id = #{id}")
    int update(PatientDO patientDO);

    @Update("UPDATE t_patient SET is_default = false WHERE user_id = #{userId}")
    int clearDefaultByUserId(Long userId);

    @Delete("DELETE FROM t_patient WHERE id = #{id}")
    int deleteById(Long id);
}
