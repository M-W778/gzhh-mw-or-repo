package com.hospital.registration.service;

import com.hospital.registration.model.entity.DepartmentDO;

import java.util.List;

public interface DepartmentService {

    List<DepartmentDO> getAllEnabledDepartments();

    List<DepartmentDO> searchDepartments(String keyword);

    DepartmentDO getDepartmentById(Long id);

}
