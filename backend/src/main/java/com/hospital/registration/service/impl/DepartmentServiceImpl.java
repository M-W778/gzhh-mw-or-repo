package com.hospital.registration.service.impl;

import com.hospital.registration.common.ErrorCode;
import com.hospital.registration.exception.BusinessException;
import com.hospital.registration.mapper.DepartmentMapper;
import com.hospital.registration.model.entity.DepartmentDO;
import com.hospital.registration.service.DepartmentService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Resource
    private DepartmentMapper departmentMapper;

    @Override
    public List<DepartmentDO> getAllEnabledDepartments() {
        return departmentMapper.findAllEnabled();
    }

    @Override
    public List<DepartmentDO> searchDepartments(String keyword) {
        return departmentMapper.searchByName(keyword);
    }

    @Override
    public DepartmentDO getDepartmentById(Long id) {
        DepartmentDO departmentDO = departmentMapper.findById(id);
        if (departmentDO == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到指定部门");
        }
        return departmentDO;
    }
}
