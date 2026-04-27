package com.hospital.registration.service.impl;

import com.hospital.registration.common.ErrorCode;
import com.hospital.registration.exception.BusinessException;
import com.hospital.registration.mapper.DoctorMapper;
import com.hospital.registration.model.entity.DoctorDO;
import com.hospital.registration.service.DoctorService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoctorServiceImpl implements DoctorService {

    @Resource
    private DoctorMapper doctorMapper;

    @Override
    public List<DoctorDO> getDoctorsByDepartment(Long departmentId) {
        return doctorMapper.findByDepartmentId(departmentId);
    }

    @Override
    public List<DoctorDO> searchDoctors(String doctorName) {
        return doctorMapper.searchByName(doctorName);
    }

    @Override
    public DoctorDO getDoctorById(Long id) {
        DoctorDO doctorDO = doctorMapper.findById(id);
        if (doctorDO == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到指定医生");
        }
        return doctorDO;
    }

    public List<DoctorDO> getAllEnabledDoctors() {
        return doctorMapper.findAllEnabled();
    }
}
