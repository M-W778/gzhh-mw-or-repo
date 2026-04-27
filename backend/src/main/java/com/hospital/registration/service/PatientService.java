package com.hospital.registration.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hospital.registration.model.dto.req.PatientReqDTO;
import com.hospital.registration.model.entity.PatientDO;

import java.util.List;

public interface PatientService extends IService<PatientDO> {

     List<PatientDO> listPatientQueryByUserId(Long userid);

     List<PatientDO> listPatientQueryByIds(Long userid, List<Long> ids);

     Long savePatient(PatientReqDTO request);

     Long updatePatient(PatientReqDTO request);

     Long deletePatient(Long patientId);

     PatientDO getDefaultPatient();

     PatientDO getPatientById(Long patientId);
}
