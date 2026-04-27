package com.hospital.registration.controller.user;

import com.hospital.registration.common.BaseResponse;
import com.hospital.registration.common.ErrorCode;
import com.hospital.registration.common.ResultUtils;
import com.hospital.registration.config.UserDetailsImpl;
import com.hospital.registration.exception.BusinessException;
import com.hospital.registration.model.dto.req.PatientReqDTO;
import com.hospital.registration.model.entity.PatientDO;
import com.hospital.registration.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    @Autowired
    private PatientService patientService;

    /**
     * 查询当前登录用户的就诊人列表。
     *
     * @param userDetails 当前登录用户信息
     * @return 就诊人列表
     */
    @GetMapping("/query")
    public BaseResponse<List<PatientDO>> listMyPatients(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return ResultUtils.success(patientService.listPatientQueryByUserId(userDetails.getId()));
    }

    /**
     * 批量查询当前登录用户的就诊人信息。
     *
     * @param ids 就诊人 ID 列表
     * @param userDetails 当前登录用户信息
     * @return 就诊人列表
     */
    @GetMapping("/query/ids")
    public BaseResponse<List<PatientDO>> listPatientQueryByIds(@RequestParam("ids") List<Long> ids,
                                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return ResultUtils.success(patientService.listPatientQueryByIds(userDetails.getId(), ids));
    }

    /**
     * 查询就诊人详情（仅当前用户可访问）。
     *
     * @param id 就诊人 ID
     * @param userDetails 当前登录用户信息
     * @return 就诊人详情
     */
    @GetMapping("/{id}")
    public BaseResponse<PatientDO> getPatientById(@PathVariable("id") Long id,
                                                  @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        PatientDO patientDO = patientService.getPatientById(id);
        if (patientDO == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        if (patientDO.getUserId() == null || !patientDO.getUserId().equals(userDetails.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return ResultUtils.success(patientDO);
    }

    /**
     * 新增就诊人。
     *
     * @param reqDTO 就诊人请求参数
     * @return 就诊人 ID
     */
    @PostMapping("/save")
    public BaseResponse<Long> savePatient(@Valid @RequestBody PatientReqDTO reqDTO) {
        return ResultUtils.success(patientService.savePatient(reqDTO));
    }

    /**
     * 更新就诊人。
     *
     * @param id 路径中的就诊人 ID
     * @param reqDTO 就诊人请求参数
     * @return 就诊人 ID
     */
    @PutMapping("/update/{id}")
    public BaseResponse<Long> updatePatient(@PathVariable(name = "id", required = false) Long id,
                                            @Valid @RequestBody PatientReqDTO reqDTO) {
        if (id != null) {
            reqDTO.setId(id);
        }
        return ResultUtils.success(patientService.updatePatient(reqDTO));
    }

    /**
     * 删除就诊人。
     *
     * @param id 路径中的就诊人 ID
     * @param patientId 请求参数中的就诊人 ID
     * @return 删除结果 ID
     */
    @DeleteMapping("/delete/{id}")
    public BaseResponse<Long> deletePatient(@PathVariable(name = "id", required = false) Long id,
                                            @RequestParam(name = "id", required = false) Long patientId) {
        Long actualId = id != null ? id : patientId;
        if (actualId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(patientService.deletePatient(actualId));
    }

    /**
     * 获取默认就诊人。
     *
     * @return 默认就诊人
     */
    @GetMapping("/default")
    public BaseResponse<PatientDO> getDefaultPatient() {
        return ResultUtils.success(patientService.getDefaultPatient());
    }
}
