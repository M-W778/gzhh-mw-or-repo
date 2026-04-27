package com.hospital.registration.controller.schedule;

import com.hospital.registration.common.BaseResponse;
import com.hospital.registration.common.ErrorCode;
import com.hospital.registration.common.ResultUtils;
import com.hospital.registration.exception.BusinessException;
import com.hospital.registration.model.dto.req.AdminScheduleCreateReqDTO;
import com.hospital.registration.model.dto.req.AdminScheduleUpdateReqDTO;
import com.hospital.registration.model.entity.ScheduleDO;
import com.hospital.registration.model.enums.ScheduleEnum;
import com.hospital.registration.model.enums.TimeSlotEnum;
import com.hospital.registration.service.ScheduleService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/schedule")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminScheduleController {

    @Resource
    private ScheduleService scheduleService;

    /**
     * 创建排班。
     *
     * @param reqDTO 创建排班请求参数
     * @return 排班 ID
     */
    @PostMapping
    public BaseResponse<Long> createSchedule(@Valid @RequestBody AdminScheduleCreateReqDTO reqDTO) {
        TimeSlotEnum slot = TimeSlotEnum.fromValue(reqDTO.getWorkPeriod());
        if (slot == null) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "workPeriod must be " + TimeSlotEnum.MORNING.getValue() + " or " + TimeSlotEnum.AFTERNOON.getValue()
            );
        }

        ScheduleDO scheduleDO = scheduleService.createSchedule(
                reqDTO.getDoctorId(),
                reqDTO.getWorkDate(),
                slot.getValue(),
                reqDTO.getTotalNum()
        );
        return ResultUtils.success(scheduleDO.getId());
    }

    /**
     * 减少排班余号（减 1）。
     *
     * @param scheduleId 排班 ID
     * @return 最新排班信息
     */
    @PostMapping("/{scheduleId}/decrease")
    public BaseResponse<ScheduleDO> decreaseAmount(@PathVariable Long scheduleId) {
        if (scheduleId == null || scheduleId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "scheduleId is invalid");
        }
        scheduleService.decreaseAmount(scheduleId);
        return ResultUtils.success(scheduleService.getScheduleById(scheduleId));
    }

    /**
     * 增加排班余号（加 1）。
     *
     * @param scheduleId 排班 ID
     * @return 最新排班信息
     */
    @PostMapping("/{scheduleId}/increase")
    public BaseResponse<ScheduleDO> increaseAmount(@PathVariable Long scheduleId) {
        if (scheduleId == null || scheduleId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "scheduleId is invalid");
        }
        scheduleService.increaseAmount(scheduleId);
        return ResultUtils.success(scheduleService.getScheduleById(scheduleId));
    }

    /**
     * 更新排班信息（号源总数与状态）。
     *
     * @param scheduleId 排班 ID
     * @param reqDTO 更新排班请求参数
     * @return 更新后的排班信息
     */
    @PutMapping("/{scheduleId}")
    public BaseResponse<ScheduleDO> updateSchedule(@PathVariable Long scheduleId,
                                                   @Valid @RequestBody AdminScheduleUpdateReqDTO reqDTO) {
        if (scheduleId == null || scheduleId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "scheduleId is invalid");
        }
        ScheduleEnum scheduleStatus = parseScheduleStatus(reqDTO.getStatus());
        if (scheduleStatus == null) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "status must be " + ScheduleEnum.AVAILABLE.getValue()
                            + ", " + ScheduleEnum.FULL.getValue()
                            + " or " + ScheduleEnum.SUSPENDED.getValue()
            );
        }
        ScheduleDO scheduleDO = scheduleService.updateSchedule(
                scheduleId,
                reqDTO.getTotalNum(),
                scheduleStatus.getValue()
        );
        return ResultUtils.success(scheduleDO);
    }

    /**
     * 删除排班。
     *
     * @param scheduleId 排班 ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{scheduleId}")
    public BaseResponse<Boolean> deleteSchedule(@PathVariable Long scheduleId) {
        if (scheduleId == null || scheduleId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "scheduleId is invalid");
        }
        scheduleService.deleteSchedule(scheduleId);
        return ResultUtils.success(true);
    }

    private ScheduleEnum parseScheduleStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        for (ScheduleEnum item : ScheduleEnum.values()) {
            if (item.getValue().equalsIgnoreCase(status)
                    || item.name().equalsIgnoreCase(status)
                    || item.getText().equalsIgnoreCase(status)) {
                return item;
            }
        }
        return null;
    }
}
