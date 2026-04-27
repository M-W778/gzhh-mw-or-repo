package com.hospital.registration.controller;

import com.hospital.registration.common.BaseResponse;
import com.hospital.registration.common.ResultUtils;
import com.hospital.registration.model.entity.DepartmentDO;
import com.hospital.registration.service.DepartmentService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/department")
@CrossOrigin(origins = "*")
public class DepartmentController {

    @Resource
    private DepartmentService departmentService;

    /**
     * 查询全部启用科室列表。
     *
     * @return 科室列表
     */
    @GetMapping
    public BaseResponse<List<DepartmentDO>> getAllDepartments() {

        return ResultUtils.success(departmentService.getAllEnabledDepartments());
    }

    /**
     * 根据科室 ID 查询科室详情。
     *
     * @param id 科室 ID
     * @return 科室详情
     */
    @GetMapping("/{id}")
    public BaseResponse<DepartmentDO> getDepartmentById(@PathVariable Long id) {
        return ResultUtils.success(departmentService.getDepartmentById(id));

    }

    /**
     * 按关键字搜索科室。
     *
     * @param keyword 通用关键字
     * @param deptName 科室名称关键字
     * @return 匹配的科室列表
     */
    @GetMapping("/search")
    public BaseResponse<List<DepartmentDO>> searchDepartments(@RequestParam(required = false) String keyword,
                                                              @RequestParam(required = false) String deptName) {
        String actual = (keyword != null && !keyword.isBlank()) ? keyword : deptName;
        return ResultUtils.success(departmentService.searchDepartments(actual == null ? "" : actual));
    }
}
