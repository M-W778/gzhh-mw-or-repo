package com.hospital.registration.service;

import com.hospital.registration.model.dto.req.AdminNoticeSaveReqDTO;
import com.hospital.registration.model.dto.resp.NoticePageRespDTO;
import com.hospital.registration.model.dto.resp.NoticeRespDTO;

public interface NoticeService {

    NoticePageRespDTO queryNoticePage(int page, int size);

    NoticePageRespDTO queryAdminNoticePage(int page, int size);

    NoticeRespDTO getNoticeById(Long id);

    Long createNotice(AdminNoticeSaveReqDTO reqDTO);

    boolean updateNotice(Long id, AdminNoticeSaveReqDTO reqDTO);

    boolean deleteNotice(Long id);
}
