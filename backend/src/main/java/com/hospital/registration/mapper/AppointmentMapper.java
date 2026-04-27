package com.hospital.registration.mapper;

import com.hospital.registration.model.entity.AppointmentDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface AppointmentMapper {

    @Select("SELECT * FROM t_appointment WHERE id = #{id}")
    @Results(id = "appointmentResultMap", value = {
            @Result(property = "appointmentNo", column = "appointment_no"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "patientId", column = "patient_id"),
            @Result(property = "doctorId", column = "doctor_id"),
            @Result(property = "departmentId", column = "department_id"),
            @Result(property = "scheduleId", column = "schedule_id"),
            @Result(property = "appointmentDate", column = "appointment_date"),
            @Result(property = "timeSlot", column = "time_slot"),
            @Result(property = "queueNumber", column = "queue_number"),
            @Result(property = "refuseTime", column = "refuse_time"),
            @Result(property = "refuseReason", column = "refuse_reason"),
            @Result(property = "notificationSent", column = "notification_sent"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updatedTime", column = "update_time")
    })
    AppointmentDO findById(Long id);

    @Select("SELECT * FROM t_appointment WHERE appointment_no = #{appointmentNo} LIMIT 1")
    @Results(value = {
            @Result(property = "appointmentNo", column = "appointment_no"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "patientId", column = "patient_id"),
            @Result(property = "doctorId", column = "doctor_id"),
            @Result(property = "departmentId", column = "department_id"),
            @Result(property = "scheduleId", column = "schedule_id"),
            @Result(property = "appointmentDate", column = "appointment_date"),
            @Result(property = "timeSlot", column = "time_slot"),
            @Result(property = "queueNumber", column = "queue_number"),
            @Result(property = "refuseTime", column = "refuse_time"),
            @Result(property = "refuseReason", column = "refuse_reason"),
            @Result(property = "notificationSent", column = "notification_sent"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updatedTime", column = "update_time")
    })
    AppointmentDO findByAppointmentNo(String appointmentNo);

    @Select("SELECT * FROM t_appointment WHERE user_id = #{userId} ORDER BY create_time DESC")
    @Results(value = {
            @Result(property = "appointmentNo", column = "appointment_no"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "patientId", column = "patient_id"),
            @Result(property = "doctorId", column = "doctor_id"),
            @Result(property = "departmentId", column = "department_id"),
            @Result(property = "scheduleId", column = "schedule_id"),
            @Result(property = "appointmentDate", column = "appointment_date"),
            @Result(property = "timeSlot", column = "time_slot"),
            @Result(property = "queueNumber", column = "queue_number"),
            @Result(property = "refuseTime", column = "refuse_time"),
            @Result(property = "refuseReason", column = "refuse_reason"),
            @Result(property = "notificationSent", column = "notification_sent"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updatedTime", column = "update_time")
    })
    List<AppointmentDO> findByUserId(Long userId);

    @Select("""
            SELECT COUNT(*)
            FROM t_appointment
            WHERE patient_id = #{patientId}
              AND appointment_date = #{date}
              AND status <> 'CANCELLED'
              AND (
                    department_id = #{departmentId}
                    OR (
                        department_id IS NULL
                        AND doctor_id IN (
                            SELECT id
                            FROM t_doctor
                            WHERE department_id = #{departmentId}
                        )
                    )
                  )
            """)
    long countActiveByPatientAndDepartmentAndDate(@Param("patientId") Long patientId,
                                                  @Param("departmentId") Long departmentId,
                                                  @Param("date") LocalDate date);

    @Select("SELECT COUNT(*) FROM t_appointment WHERE schedule_id = #{scheduleId} AND status <> 'CANCELLED'")
    long countActiveByScheduleId(Long scheduleId);

    @Insert("""
            INSERT INTO t_appointment
            (appointment_no, user_id, patient_id, doctor_id, department_id, schedule_id, appointment_date, time_slot,
             status, queue_number, remarks, notification_sent, create_time, update_time)
            VALUES
            (#{appointmentNo}, #{userId}, #{patientId}, #{doctorId}, #{departmentId}, #{scheduleId}, #{appointmentDate}, #{timeSlot},
             #{status}, #{queueNumber}, #{remarks}, #{notificationSent}, NOW(), NOW())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AppointmentDO appointmentDO);

    @Update("""
            UPDATE t_appointment
            SET status = #{status},
                refuse_time = #{refuseTime},
                refuse_reason = #{refuseReason},
                update_time = NOW()
            WHERE id = #{id}
            """)
    int updateStatus(AppointmentDO appointmentDO);

    @Update("UPDATE t_appointment SET notification_sent = true, update_time = NOW() WHERE id = #{id}")
    int markNotificationSent(Long id);

    @Update("UPDATE t_appointment SET notification_sent = #{notificationSent}, update_time = NOW() WHERE id = #{id}")
    int updateNotificationSent(AppointmentDO appointmentDO);

    @Delete("DELETE FROM t_appointment WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM t_appointment WHERE doctor_id = #{doctorId} ORDER BY appointment_date DESC, time_slot ASC")
    @Results(value = {
            @Result(property = "appointmentNo", column = "appointment_no"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "patientId", column = "patient_id"),
            @Result(property = "doctorId", column = "doctor_id"),
            @Result(property = "departmentId", column = "department_id"),
            @Result(property = "scheduleId", column = "schedule_id"),
            @Result(property = "appointmentDate", column = "appointment_date"),
            @Result(property = "timeSlot", column = "time_slot"),
            @Result(property = "queueNumber", column = "queue_number"),
            @Result(property = "refuseTime", column = "refuse_time"),
            @Result(property = "refuseReason", column = "refuse_reason"),
            @Result(property = "notificationSent", column = "notification_sent"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updatedTime", column = "update_time")
    })
    List<AppointmentDO> findByDoctorId(Long doctorId);

    @Select("SELECT * FROM t_appointment WHERE doctor_id = #{doctorId} AND appointment_date = #{date} AND status = 'PENDING' ORDER BY time_slot ASC")
    @Results(value = {
            @Result(property = "appointmentNo", column = "appointment_no"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "patientId", column = "patient_id"),
            @Result(property = "doctorId", column = "doctor_id"),
            @Result(property = "departmentId", column = "department_id"),
            @Result(property = "scheduleId", column = "schedule_id"),
            @Result(property = "appointmentDate", column = "appointment_date"),
            @Result(property = "timeSlot", column = "time_slot"),
            @Result(property = "queueNumber", column = "queue_number"),
            @Result(property = "refuseTime", column = "refuse_time"),
            @Result(property = "refuseReason", column = "refuse_reason"),
            @Result(property = "notificationSent", column = "notification_sent"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updatedTime", column = "update_time")
    })
    List<AppointmentDO> findByDoctorIdAndDate(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);

    @Select("SELECT * FROM t_appointment WHERE doctor_id = #{doctorId} AND status = #{status} ORDER BY appointment_date DESC")
    @Results(value = {
            @Result(property = "appointmentNo", column = "appointment_no"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "patientId", column = "patient_id"),
            @Result(property = "doctorId", column = "doctor_id"),
            @Result(property = "departmentId", column = "department_id"),
            @Result(property = "scheduleId", column = "schedule_id"),
            @Result(property = "appointmentDate", column = "appointment_date"),
            @Result(property = "timeSlot", column = "time_slot"),
            @Result(property = "queueNumber", column = "queue_number"),
            @Result(property = "refuseTime", column = "refuse_time"),
            @Result(property = "refuseReason", column = "refuse_reason"),
            @Result(property = "notificationSent", column = "notification_sent"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updatedTime", column = "update_time")
    })
    List<AppointmentDO> findByDoctorIdAndStatus(@Param("doctorId") Long doctorId, @Param("status") String status);

    @Select("SELECT COUNT(*) FROM t_appointment WHERE DATE(create_time) = CURDATE()")
    long countTodayAppointments();

    @Select("SELECT COUNT(*) FROM t_appointment WHERE DATE(create_time) = CURDATE() AND status = 'PENDING'")
    long countTodayPendingAppointments();

    @Select("SELECT COUNT(*) FROM t_appointment WHERE status = 'PENDING'")
    long countTotalPendingAppointments();

    @Select("SELECT COUNT(*) FROM t_appointment WHERE status = 'COMPLETED'")
    long countTotalCompletedAppointments();

    @Select("SELECT COUNT(*) FROM t_appointment WHERE status = 'CANCELLED'")
    long countTotalCancelledAppointments();

    @Select("""
            SELECT d.dept_name AS departmentName, COUNT(*) AS count
            FROM t_appointment a
            JOIN t_doctor doc ON a.doctor_id = doc.id
            JOIN t_department d ON doc.department_id = d.id
            WHERE a.create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
            GROUP BY d.id, d.dept_name
            ORDER BY count DESC
            LIMIT 5
            """)
    @Results({
            @Result(property = "departmentName", column = "departmentName"),
            @Result(property = "count", column = "count")
    })
    List<DepartmentStat> findTopDepartmentsLast7Days();

    @Select("""
            SELECT DATE(create_time) AS date, COUNT(*) AS count
            FROM t_appointment
            WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
            GROUP BY DATE(create_time)
            ORDER BY date ASC
            """)
    @Results({
            @Result(property = "date", column = "date"),
            @Result(property = "count", column = "count")
    })
    List<DailyStat> findDailyAppointmentsLast7Days();

    class DepartmentStat {
        private String departmentName;
        private Long count;

        public String getDepartmentName() {
            return departmentName;
        }

        public void setDepartmentName(String departmentName) {
            this.departmentName = departmentName;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }
    }

    class DailyStat {
        private java.sql.Date date;
        private Long count;

        public java.sql.Date getDate() {
            return date;
        }

        public void setDate(java.sql.Date date) {
            this.date = date;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }
    }
}
