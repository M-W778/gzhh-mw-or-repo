package com.hospital.registration.mapper;

import com.hospital.registration.model.entity.ScheduleDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ScheduleMapper {

    @Select("SELECT * FROM t_schedule WHERE id = #{id}")
    ScheduleDO findById(Long id);

    @Select("""
            SELECT *
            FROM t_schedule
            WHERE doctor_id = #{doctorId}
              AND schedule_date BETWEEN #{startDate} AND #{endDate}
            ORDER BY schedule_date ASC,
                     CASE time_slot
                         WHEN 'MORNING' THEN 1
                         WHEN 'AFTERNOON' THEN 2
                         ELSE 3
                     END ASC
            """)
    List<ScheduleDO> findByDoctorIdAndDateRange(@Param("doctorId") Long doctorId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    @Select("""
            SELECT *
            FROM t_schedule
            WHERE doctor_id = #{doctorId}
              AND schedule_date >= #{date}
            ORDER BY schedule_date ASC,
                     CASE time_slot
                         WHEN 'MORNING' THEN 1
                         WHEN 'AFTERNOON' THEN 2
                         ELSE 3
                     END ASC
            """)
    List<ScheduleDO> findFutureByDoctorId(@Param("doctorId") Long doctorId,
                                          @Param("date") LocalDate date);

    @Select("""
            SELECT *
            FROM t_schedule
            WHERE doctor_id = #{doctorId}
              AND schedule_date = #{date}
              AND time_slot = #{timeSlot}
            LIMIT 1
            """)
    ScheduleDO findByDoctorAndDateAndSlot(@Param("doctorId") Long doctorId,
                                          @Param("date") LocalDate date,
                                          @Param("timeSlot") String timeSlot);

    @Insert("""
            INSERT INTO t_schedule
            (doctor_id, schedule_date, time_slot, total_amount, remaining_amount, status, version, create_time, update_time)
            VALUES
            (#{doctorId}, #{scheduleDate}, #{timeSlot}, #{totalAmount}, #{remainingAmount}, #{status}, 0, NOW(), NOW())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ScheduleDO scheduleDO);

    @Update("""
            UPDATE t_schedule
            SET total_amount = #{totalAmount},
                remaining_amount = #{remainingAmount},
                status = #{status},
                version = version + 1,
                update_time = NOW()
            WHERE id = #{id}
            """)
    int update(ScheduleDO scheduleDO);

    @Update("UPDATE t_schedule SET remaining_amount = remaining_amount - 1 WHERE id = #{id} AND remaining_amount > 0")
    int decreaseQuota(Long id);

    @Update("UPDATE t_schedule SET remaining_amount = remaining_amount + 1, update_time = NOW() WHERE id = #{id}")
    int increaseQuota(Long id);

    @Delete("DELETE FROM t_schedule WHERE id = #{id}")
    void deleteById(Long id);
}
