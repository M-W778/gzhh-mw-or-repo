package com.hospital.registration.controller.user;

import com.hospital.registration.common.BaseResponse;
import com.hospital.registration.common.ResultUtils;
import com.hospital.registration.model.dto.req.AdminCreateUserReqDTO;
import com.hospital.registration.service.UserService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/user")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    @Resource
    private UserService userService;

    /**
     * 管理员创建账号（支持 ADMIN / DOCTOR）。
     * 当 role=DOCTOR 时，doctorId 必填，并自动与医生档案绑定。
     *
     * @param reqDTO 创建账号请求
     * @return 新账号 userId
     */
    @PostMapping("/create")
    public BaseResponse<Long> createAccount(@Valid @RequestBody AdminCreateUserReqDTO reqDTO) {
        return ResultUtils.success(userService.createAccountByAdmin(reqDTO));
    }
}
