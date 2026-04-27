package com.hospital.registration.controller;

import com.hospital.registration.common.BaseResponse;
import com.hospital.registration.common.ResultUtils;
import com.hospital.registration.model.dto.resp.NoticePageRespDTO;
import com.hospital.registration.service.NoticeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notice")
public class NoticeController {

    @Resource
    private NoticeService noticeService;

    @GetMapping("/page")
    public BaseResponse<NoticePageRespDTO> queryNoticePage(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "6") int size) {
        return ResultUtils.success(noticeService.queryNoticePage(page, size));
    }
}

