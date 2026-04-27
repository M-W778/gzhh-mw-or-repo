package com.hospital.registration.controller.user;

import com.hospital.registration.common.BaseResponse;
import com.hospital.registration.common.ResultUtils;
import com.hospital.registration.model.entity.DepartmentDO;
import com.hospital.registration.model.entity.DoctorDO;
import com.hospital.registration.service.DepartmentService;
import com.hospital.registration.service.DoctorService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

@RestController
@RequestMapping("/api/doctor")
@CrossOrigin(origins = "*")
public class DoctorController {

    @Resource
    private DoctorService doctorService;

    @Resource
    private DepartmentService departmentService;

    /**
     * 查询全部启用医生列表。
     *
     * @return 医生列表
     */
    @GetMapping
    public BaseResponse<List<DoctorDO>> getAllDoctors() {
        return ResultUtils.success(doctorService.getAllEnabledDoctors());
    }

    /**
     * 根据医生 ID 查询医生详情。
     *
     * @param id 医生 ID
     * @return 医生详情
     */
    @GetMapping("/{id}")
    public BaseResponse<DoctorDO> getDoctorById(@PathVariable Long id) {
        return ResultUtils.success(doctorService.getDoctorById(id));
    }

    /**
     * 根据科室 ID 查询医生列表。
     *
     * @param departmentId 科室 ID
     * @return 医生列表
     */
    @GetMapping("/department/{departmentId}")
    public BaseResponse<List<DoctorDO>> getDoctorsByDepartment(@PathVariable Long departmentId) {
        return ResultUtils.success(doctorService.getDoctorsByDepartment(departmentId));
    }

    /**
     * 按关键字搜索医生。
     *
     * @param keyword 通用关键字
     * @param doctorName 医生姓名关键字
     * @return 医生列表
     */
    @GetMapping("/search")
    public BaseResponse<List<DoctorDO>> searchDoctors(@RequestParam(required = false) String keyword,
                                                      @RequestParam(required = false) String doctorName) {
        String actual = (keyword != null && !keyword.isBlank()) ? keyword : doctorName;
        return ResultUtils.success(doctorService.searchDoctors(actual == null ? "" : actual));
    }

    /**
     * 查询医生详情（含科室信息）。
     *
     * @param id 医生 ID
     * @return 医生详情及扩展信息
     */
    @GetMapping("/detail/{id}")
    public BaseResponse<Map<String, Object>> getDoctorDetail(@PathVariable Long id) {
        DoctorDO doctorDO = doctorService.getDoctorById(id);

        DepartmentDO departmentDO = null;
        if (doctorDO.getDepartmentId() != null) {
            departmentDO = departmentService.getDepartmentById(doctorDO.getDepartmentId());
        }
        Map<String, Object> detail = new HashMap<>();
        detail.put("id", doctorDO.getId());
        detail.put("name", doctorDO.getName());
        detail.put("title", doctorDO.getTitle());
        detail.put("titleText", toTitleText(doctorDO.getTitle()));
        detail.put("specialty", doctorDO.getSpecialty());
        detail.put("introduction", doctorDO.getIntroduction());
        detail.put("avatarUrl", doctorDO.getAvatarUrl());
        detail.put("registrationFee", doctorDO.getRegistrationFee());
        detail.put("departmentId", doctorDO.getDepartmentId());
        detail.put("department", departmentDO);
        return ResultUtils.success(detail);
    }

    private String toTitleText(String title) {
        if (title == null || title.isBlank()) {
            return "";
        }
        String normalized = title.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "CHIEF", "CHIEF_PHYSICIAN" -> "主任医师";
            case "ASSOCIATE_CHIEF" -> "副主任医师";
            case "ATTENDING" -> "主治医师";
            case "RESIDENT" -> "住院医师";
            case "INTERN" -> "实习医师";
            default -> title;
        };
    }
}
