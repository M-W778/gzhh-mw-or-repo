package com.hospital.registration.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.registration.common.ErrorCode;
import com.hospital.registration.config.JwtUtils;
import com.hospital.registration.config.UserDetailsImpl;
import com.hospital.registration.exception.BusinessException;
import com.hospital.registration.mapper.DoctorMapper;
import com.hospital.registration.mapper.UserMapper;
import com.hospital.registration.model.dto.req.AdminCreateUserReqDTO;
import com.hospital.registration.model.dto.req.UserLoginReqDTO;
import com.hospital.registration.model.dto.req.UserRegisterReqDTO;
import com.hospital.registration.model.dto.resp.UserLoginRespDTO;
import com.hospital.registration.model.entity.DoctorDO;
import com.hospital.registration.model.entity.UserDO;
import com.hospital.registration.model.enums.UserRoleEnum;
import com.hospital.registration.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private DoctorMapper doctorMapper;

    @Override
    public Long register(UserRegisterReqDTO reqDTO) {
        String username = reqDTO.getUsername();
        String password = reqDTO.getPassword();
        String checkPassword = reqDTO.getCheckPassword();
        String phone = reqDTO.getPhone();
        String email = reqDTO.getEmail();

        if (StringUtils.isAnyBlank(username, password, checkPassword, phone, email)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "非法参数");
        }
        if (!password.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        return createUserAccount(username, password, phone, email, UserRoleEnum.PATIENT);
    }

    @Override
    public UserLoginRespDTO login(UserLoginReqDTO reqDTO, HttpServletRequest request) {
        String account = reqDTO.getUsername();
        String password = reqDTO.getPassword();

        if (StringUtils.isAnyBlank(account, password)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "非法参数");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(account, password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateJwtToken(authentication);
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetailsImpl userDetails)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败");
        }

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return UserLoginRespDTO.builder()
                .userId(String.valueOf(userDetails.getId()))
                .id(String.valueOf(userDetails.getId()))
                .username(userDetails.getUsername())
                .realName(userDetails.getUsername())
                .accessToken(jwt)
                .token(jwt)
                .phone(userDetails.getPhone())
                .roles(roles)
                .build();
    }



    @Override
    public UserDO findById(Long id) {
        return userMapper.findById(id);
    }

    @Override
    public boolean hasUsername(String username) {
        if (StringUtils.isBlank(username)) {
            return false;
        }
        return userMapper.hasUsername(username);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAccountByAdmin(AdminCreateUserReqDTO reqDTO) {
        if (reqDTO == null || StringUtils.isAnyBlank(
                reqDTO.getUsername(),
                reqDTO.getPassword(),
                reqDTO.getPhone(),
                reqDTO.getEmail(),
                reqDTO.getRole())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "非法参数");
        }

        UserRoleEnum roleEnum = UserRoleEnum.fromValueOrText(reqDTO.getRole());
        if (roleEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "role must be ADMIN or DOCTOR");
        }
        if (roleEnum == UserRoleEnum.PATIENT || roleEnum == UserRoleEnum.BAN) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "role must be ADMIN or DOCTOR");
        }

        if (roleEnum == UserRoleEnum.DOCTOR) {
            if (reqDTO.getDoctorId() == null || reqDTO.getDoctorId() <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "doctorId is required when role is DOCTOR");
            }
            DoctorDO doctorDO = doctorMapper.findById(reqDTO.getDoctorId());
            if (doctorDO == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "doctor not found");
            }
            if (doctorDO.getUserId() != null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "doctor already bound to another account");
            }
        }

        Long userId = createUserAccount(
                reqDTO.getUsername(),
                reqDTO.getPassword(),
                reqDTO.getPhone(),
                reqDTO.getEmail(),
                roleEnum
        );

        if (roleEnum == UserRoleEnum.DOCTOR) {
            int updated = doctorMapper.bindUser(reqDTO.getDoctorId(), userId);
            if (updated <= 0) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "bind doctor account failed");
            }
        }
        return userId;
    }

    private Long createUserAccount(String username, String rawPassword, String phone, String email, UserRoleEnum roleEnum) {
        validateUniqueUser(username, phone, email);

        UserDO userDO = new UserDO();
        userDO.setUsername(username);
        userDO.setPassword(passwordEncoder.encode(rawPassword));
        userDO.setPhone(phone);
        userDO.setEmail(email);
        userDO.setRole(roleEnum.getValue());
        userDO.setDeletionTime(0L);
        userDO.setDelFlag(0);
        userDO.setCreateTime(LocalDateTime.now());
        userDO.setUpdateTime(LocalDateTime.now());

        int inserted = userMapper.insert(userDO);
        if (inserted <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "save user failed");
        }
        return userDO.getId();
    }

    private void validateUniqueUser(String username, String phone, String email) {
        if (userMapper.hasUsername(username)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名已存在");
        }
        if (userMapper.existsByPhone(phone)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号已存在");
        }
        if (userMapper.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱已存在");
        }
    }
}
