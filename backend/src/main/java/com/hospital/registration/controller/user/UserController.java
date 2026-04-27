package com.hospital.registration.controller.user;

import com.hospital.registration.common.BaseResponse;
import com.hospital.registration.common.ErrorCode;
import com.hospital.registration.common.ResultUtils;
import com.hospital.registration.exception.BusinessException;
import com.hospital.registration.model.dto.req.UserLoginReqDTO;
import com.hospital.registration.model.dto.req.UserRegisterReqDTO;
import com.hospital.registration.model.dto.resp.UserLoginRespDTO;
import com.hospital.registration.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册。
     *
     * @param reqDTO 注册请求参数
     * @return 用户 ID
     */
    @PostMapping("/register")
    public BaseResponse<Long> register(@RequestBody UserRegisterReqDTO reqDTO) {
        if (reqDTO == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.isAnyBlank(reqDTO.getUsername(), reqDTO.getPassword(), reqDTO.getCheckPassword(), reqDTO.getPhone(), reqDTO.getEmail())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(userService.register(reqDTO));
    }

    /**
     * 用户登录。
     *
     * @param reqDTO 登录请求参数
     * @param request Http 请求对象
     * @return 登录结果
     */
    @PostMapping("/login")
    public BaseResponse<UserLoginRespDTO> login(@RequestBody UserLoginReqDTO reqDTO, HttpServletRequest request) {
        if (reqDTO == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.isAnyBlank(reqDTO.getUsername(), reqDTO.getPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(userService.login(reqDTO, request));
    }

    /**
     * 检查用户名是否已存在。
     *
     * @param username 用户名
     * @return 是否已存在
     */
    @GetMapping("/has-username")
    public BaseResponse<Boolean> hasUsername(@RequestParam("username") String username) {
        if (StringUtils.isBlank(username)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(userService.hasUsername(username));
    }
}
