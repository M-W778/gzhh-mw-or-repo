package com.hospital.registration.service.impl;

import cn.hutool.core.util.IdcardUtil;
import cn.hutool.core.util.PhoneUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.registration.common.ErrorCode;
import com.hospital.registration.config.UserDetailsImpl;
import com.hospital.registration.exception.BusinessException;
import com.hospital.registration.mapper.PatientMapper;
import com.hospital.registration.model.dto.req.PatientReqDTO;
import com.hospital.registration.model.entity.PatientDO;
import com.hospital.registration.service.PatientService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, PatientDO> implements PatientService {

    @Resource
    private PatientMapper patientMapper;

    @Override
    public List<PatientDO> listPatientQueryByUserId(Long userId) {
        return patientMapper.queryByUserId(userId);
    }

    @Override
    public List<PatientDO> listPatientQueryByIds(Long userId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return patientMapper.queryByUserId(userId)
                .stream()
                .filter(p -> ids.contains(p.getId()))
                .collect(Collectors.toList());
    }

    private void verifyPatient(PatientReqDTO reqDTO) {
        if (reqDTO == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "非法参数");
        }
        if (StringUtils.isBlank(reqDTO.getRealName()) || reqDTO.getRealName().length() < 2 || reqDTO.getRealName().length() > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "姓名无效");
        }
        if (!IdcardUtil.isValidCard(reqDTO.getIdCard())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "身份证号码无效");
        }
        if (!PhoneUtil.isMobile(reqDTO.getPhone())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号码无效");
        }
        if (reqDTO.getGender() == null || (!Objects.equals(reqDTO.getGender(), 0) && !Objects.equals(reqDTO.getGender(), 1))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "性别有误");
        }
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            return userDetails.getId();
        }
        return null;
    }

    @Override
    @Transactional
    public Long savePatient(PatientReqDTO reqDTO) {
        verifyPatient(reqDTO);

        Long userId = currentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未登录");
        }

        if (patientMapper.existsByUserIdAndIdCard(userId, reqDTO.getIdCard())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "身份证号码已存在");
        }

        long count = patientMapper.countByUserId(userId);
        boolean isDefault = count == 0 || Boolean.TRUE.equals(reqDTO.getIsDefault());
        if (isDefault) {
            patientMapper.clearDefaultByUserId(userId);
        }

        PatientDO patientDO = new PatientDO();
        patientDO.setUserId(userId);
        patientDO.setRealName(reqDTO.getRealName());
        patientDO.setIdCard(reqDTO.getIdCard());
        patientDO.setPhone(reqDTO.getPhone());
        patientDO.setGender(reqDTO.getGender());
        patientDO.setBirthDate(reqDTO.getBirthDate());
        patientDO.setIsDefault(isDefault);
        patientDO.setCreateTime(LocalDateTime.now());
        patientDO.setUpdateTime(LocalDateTime.now());

        patientMapper.insert(patientDO);
        return patientDO.getId();
    }

    @Override
    @Transactional
    public Long updatePatient(PatientReqDTO reqDTO) {
        verifyPatient(reqDTO);

        Long userId = currentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未登录");
        }
        if (reqDTO.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "需要id");
        }

        PatientDO patientDO = patientMapper.findById(reqDTO.getId());
        if (patientDO == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到指定患者");
        }
        if (!userId.equals(patientDO.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无访问权限");
        }

        if (!Objects.equals(patientDO.getIdCard(), reqDTO.getIdCard())
                && patientMapper.existsByUserIdAndIdCard(userId, reqDTO.getIdCard())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "身份证号码已存在");
        }

        boolean isDefault = Boolean.TRUE.equals(reqDTO.getIsDefault());
        if (isDefault) {
            patientMapper.clearDefaultByUserId(userId);
        }

        patientDO.setRealName(reqDTO.getRealName());
        patientDO.setIdCard(reqDTO.getIdCard());
        patientDO.setPhone(reqDTO.getPhone());
        patientDO.setGender(reqDTO.getGender());
        patientDO.setBirthDate(reqDTO.getBirthDate());
        patientDO.setIsDefault(isDefault);
        patientDO.setUpdateTime(LocalDateTime.now());

        patientMapper.update(patientDO);
        return patientDO.getId();
    }

    @Override
    @Transactional
    public Long deletePatient(Long patientId) {
        Long userId = currentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未登录");
        }

        PatientDO patientDO = patientMapper.findById(patientId);
        if (patientDO == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到指定患者");
        }
        if (!userId.equals(patientDO.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无访问权限");
        }

        patientMapper.deleteById(patientId);
        return patientId;
    }

    @Override
    public PatientDO getDefaultPatient() {
        Long userId = currentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未登录");
        }
        PatientDO patientDO = patientMapper.findDefaultByUserId(userId);
        if (patientDO != null) {
            return patientDO;
        }
        List<PatientDO> list = patientMapper.queryByUserId(userId);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public PatientDO getPatientById(Long patientId) {
        PatientDO patientDO = patientMapper.findById(patientId);
        if (patientDO == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到指定患者");
        }
        return patientDO;
    }
}
