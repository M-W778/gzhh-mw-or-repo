package com.hospital.registration.controller.notice;

import com.hospital.registration.common.BaseResponse;
import com.hospital.registration.common.ResultUtils;
import com.hospital.registration.model.dto.req.AdminNoticeSaveReqDTO;
import com.hospital.registration.model.dto.resp.NoticePageRespDTO;
import com.hospital.registration.model.dto.resp.NoticeRespDTO;
import com.hospital.registration.service.NoticeService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/notice")
@PreAuthorize("hasRole('ADMIN')")
public class AdminNoticeController {

    @Resource
    private NoticeService noticeService;

    @GetMapping("/page")
    public BaseResponse<NoticePageRespDTO> queryNoticePage(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return ResultUtils.success(noticeService.queryAdminNoticePage(page, size));
    }

    @GetMapping("/{id}")
    public BaseResponse<NoticeRespDTO> getNoticeById(@PathVariable Long id) {
        return ResultUtils.success(noticeService.getNoticeById(id));
    }

    @PostMapping
    public BaseResponse<Long> createNotice(@Valid @RequestBody AdminNoticeSaveReqDTO reqDTO) {
        return ResultUtils.success(noticeService.createNotice(reqDTO));
    }

    @PutMapping("/{id}")
    public BaseResponse<Boolean> updateNotice(@PathVariable Long id, @Valid @RequestBody AdminNoticeSaveReqDTO reqDTO) {
        return ResultUtils.success(noticeService.updateNotice(id, reqDTO));
    }

    @DeleteMapping("/{id}")
    public BaseResponse<Boolean> deleteNotice(@PathVariable Long id) {
        return ResultUtils.success(noticeService.deleteNotice(id));
    }
}
