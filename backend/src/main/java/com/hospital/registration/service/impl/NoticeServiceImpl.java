package com.hospital.registration.service.impl;

import com.hospital.registration.common.ErrorCode;
import com.hospital.registration.exception.BusinessException;
import com.hospital.registration.mapper.NoticeMapper;
import com.hospital.registration.model.dto.req.AdminNoticeSaveReqDTO;
import com.hospital.registration.model.dto.resp.NoticePageRespDTO;
import com.hospital.registration.model.dto.resp.NoticeRespDTO;
import com.hospital.registration.model.entity.NoticeDO;
import com.hospital.registration.service.NoticeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class NoticeServiceImpl implements NoticeService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Set<String> TAG_TYPE_SET = Set.of("primary", "success", "warning", "info", "danger");

    @Resource
    private NoticeMapper noticeMapper;

    @Override
    public NoticePageRespDTO queryNoticePage(int page, int size) {
        validatePagination(page, size, 50);
        return buildPage(page, size, true);
    }

    @Override
    public NoticePageRespDTO queryAdminNoticePage(int page, int size) {
        validatePagination(page, size, 100);
        return buildPage(page, size, null);
    }

    @Override
    public NoticeRespDTO getNoticeById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "noticeId is invalid");
        }
        NoticeDO noticeDO = noticeMapper.findById(id);
        if (noticeDO == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "notice not found");
        }
        return toResp(noticeDO);
    }

    @Override
    public Long createNotice(AdminNoticeSaveReqDTO reqDTO) {
        NoticeDO noticeDO = toNoticeDO(reqDTO);
        int inserted = noticeMapper.insert(noticeDO);
        if (inserted <= 0 || noticeDO.getId() == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "create notice failed");
        }
        return noticeDO.getId();
    }

    @Override
    public boolean updateNotice(Long id, AdminNoticeSaveReqDTO reqDTO) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "noticeId is invalid");
        }
        NoticeDO existed = noticeMapper.findById(id);
        if (existed == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "notice not found");
        }

        NoticeDO updateDO = toNoticeDO(reqDTO);
        updateDO.setId(id);
        int updated = noticeMapper.update(updateDO);
        if (updated <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "update notice failed");
        }
        return true;
    }

    @Override
    public boolean deleteNotice(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "noticeId is invalid");
        }
        NoticeDO existed = noticeMapper.findById(id);
        if (existed == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "notice not found");
        }
        int deleted = noticeMapper.softDeleteById(id);
        if (deleted <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "delete notice failed");
        }
        return true;
    }

    private void validatePagination(int page, int size, int maxSize) {
        if (page <= 0 || size <= 0 || size > maxSize) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "invalid pagination params");
        }
    }

    private NoticePageRespDTO buildPage(int page, int size, Boolean enabled) {
        int offset = (page - 1) * size;
        List<NoticeRespDTO> records = noticeMapper.findPage(offset, size, enabled).stream()
                .map(this::toResp)
                .toList();
        long total = noticeMapper.countPage(enabled);
        boolean hasMore = offset + records.size() < total;
        return NoticePageRespDTO.builder()
                .records(records)
                .total(total)
                .page(page)
                .size(size)
                .hasMore(hasMore)
                .build();
    }

    private NoticeRespDTO toResp(NoticeDO noticeDO) {
        return NoticeRespDTO.builder()
                .id(noticeDO.getId())
                .tag(noticeDO.getTag())
                .tagType(noticeDO.getTagType())
                .title(noticeDO.getTitle())
                .summary(noticeDO.getSummary())
                .publishDate(noticeDO.getPublishDate() == null ? null : noticeDO.getPublishDate().format(DATE_FORMATTER))
                .pinned(Boolean.TRUE.equals(noticeDO.getPinned()))
                .path(noticeDO.getPath())
                .enabled(Boolean.TRUE.equals(noticeDO.getEnabled()))
                .build();
    }

    private NoticeDO toNoticeDO(AdminNoticeSaveReqDTO reqDTO) {
        NoticeDO noticeDO = new NoticeDO();
        noticeDO.setTag(reqDTO.getTag().trim());
        noticeDO.setTagType(normalizeTagType(reqDTO.getTagType()));
        noticeDO.setTitle(reqDTO.getTitle().trim());
        noticeDO.setSummary(reqDTO.getSummary() == null ? null : reqDTO.getSummary().trim());
        noticeDO.setPublishDate(reqDTO.getPublishDate());
        noticeDO.setPinned(Boolean.TRUE.equals(reqDTO.getPinned()));
        noticeDO.setPath(normalizePath(reqDTO.getPath()));
        noticeDO.setEnabled(Boolean.TRUE.equals(reqDTO.getEnabled()));
        return noticeDO;
    }

    private String normalizeTagType(String tagType) {
        String value = tagType == null ? "" : tagType.trim().toLowerCase(Locale.ROOT);
        if (!TAG_TYPE_SET.contains(value)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "invalid tagType");
        }
        return value;
    }

    private String normalizePath(String path) {
        if (path == null) {
            return null;
        }
        String value = path.trim();
        return value.isEmpty() ? null : value;
    }
}
