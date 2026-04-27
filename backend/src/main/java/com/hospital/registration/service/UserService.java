package com.hospital.registration.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.hospital.registration.model.dto.req.AdminCreateUserReqDTO;
import com.hospital.registration.model.dto.req.UserLoginReqDTO;
import com.hospital.registration.model.dto.req.UserRegisterReqDTO;
import com.hospital.registration.model.dto.resp.UserLoginRespDTO;
import com.hospital.registration.model.entity.UserDO;
import jakarta.servlet.http.HttpServletRequest;


public interface UserService extends IService<UserDO> {

    Long register(UserRegisterReqDTO reqDTO);

    UserLoginRespDTO login(UserLoginReqDTO reqDTO, HttpServletRequest request);


    UserDO findById(Long id);

    boolean hasUsername(String username);

    Long createAccountByAdmin(AdminCreateUserReqDTO reqDTO);

}
