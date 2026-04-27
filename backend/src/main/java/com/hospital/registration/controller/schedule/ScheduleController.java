package com.hospital.registration.controller.schedule;

import com.hospital.registration.common.BaseResponse;
import com.hospital.registration.common.ErrorCode;
import com.hospital.registration.common.ResultUtils;
import com.hospital.registration.exception.BusinessException;
import com.hospital.registration.model.dto.resp.ScheduleRespDTO;
import com.hospital.registration.model.entity.ScheduleDO;
import com.hospital.registration.model.enums.ScheduleEnum;
import com.hospital.registration.model.enums.TimeSlotEnum;
import com.hospital.registration.service.ScheduleService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/schedule")
@CrossOrigin(origins = "*")
public class ScheduleController {

    @Resource
    private ScheduleService scheduleService;

    /**
     * 查询医生未来 7 天排班（含当天）。
     *
     * @param doctorId 医生 ID
     * @return 排班列表
     */
    @GetMapping("/{doctorId}")
    public BaseResponse<List<ScheduleRespDTO>> listDoctorSchedule7Days(@PathVariable Long doctorId) {
        if (doctorId == null || doctorId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "doctorId is invalid");
        }
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(6);
        List<ScheduleDO> list = scheduleService.getSchedulesByDoctorAndDateRange(doctorId, start, end);
        return ResultUtils.success(list.stream().map(this::toResp).collect(Collectors.toList()));
    }

    /**
     * 查询医生未来全部排班（含当天）。
     *
     * @param doctorId 医生 ID
     * @return 排班列表
     */
    @GetMapping("/future/{doctorId}")
    public BaseResponse<List<ScheduleRespDTO>> listDoctorFutureSchedules(@PathVariable Long doctorId) {
        if (doctorId == null || doctorId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "doctorId is invalid");
        }
        List<ScheduleDO> list = scheduleService.getFutureSchedulesByDoctor(doctorId);
        return ResultUtils.success(list.stream().map(this::toResp).collect(Collectors.toList()));
    }

    /**
     * 根据排班 ID 查询排班详情。
     *
     * @param scheduleId 排班 ID
     * @return 排班详情
     */
    @GetMapping("/detail/{scheduleId}")
    public BaseResponse<ScheduleRespDTO> getScheduleDetail(@PathVariable Long scheduleId) {
        if (scheduleId == null || scheduleId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "scheduleId is invalid");
        }
        ScheduleDO scheduleDO = scheduleService.getScheduleById(scheduleId);
        return ResultUtils.success(toResp(scheduleDO));
    }

    private ScheduleRespDTO toResp(ScheduleDO scheduleDO) {
        ScheduleRespDTO resp = new ScheduleRespDTO();
        resp.setId(scheduleDO.getId());
        resp.setDoctorId(scheduleDO.getDoctorId());
        resp.setWorkDate(scheduleDO.getScheduleDate());

        String workPeriod = scheduleDO.getTimeSlot() == null ? null : scheduleDO.getTimeSlot();
        resp.setWorkPeriod(workPeriod);
        resp.setWorkPeriodText(toWorkPeriodText(workPeriod));

        resp.setTotalNum(scheduleDO.getTotalAmount());
        resp.setRemainingNum(scheduleDO.getRemainingAmount());

        String status = scheduleDO.getStatus() == null ? null : scheduleDO.getStatus();
        resp.setStatus(status);
        resp.setStatusText(toStatusText(status));
        return resp;
    }

    private String toWorkPeriodText(String workPeriod) {
        TimeSlotEnum slotEnum = TimeSlotEnum.fromValue(workPeriod);
        return slotEnum == null ? workPeriod : slotEnum.getText();
    }

    private String toStatusText(String status) {
        ScheduleEnum statusEnum = ScheduleEnum.getEnumByValue(status);
        return statusEnum == null ? status : statusEnum.getText();
    }
}
