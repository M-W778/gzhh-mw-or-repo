package com.hospital.registration.model.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Paged notice response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticePageRespDTO {

    private List<NoticeRespDTO> records;

    private long total;

    private int page;

    private int size;

    private boolean hasMore;
}

