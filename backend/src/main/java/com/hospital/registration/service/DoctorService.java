package com.hospital.registration.service;

import com.hospital.registration.model.entity.DoctorDO;

import java.util.List;

public interface DoctorService {

    List<DoctorDO> getDoctorsByDepartment(Long departmentId);

    List<DoctorDO> searchDoctors(String doctorName);

    DoctorDO getDoctorById(Long id);

    List<DoctorDO> getAllEnabledDoctors();


}
