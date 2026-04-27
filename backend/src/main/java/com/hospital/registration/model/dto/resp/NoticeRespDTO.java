package com.hospital.registration.model.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Notice item for homepage.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeRespDTO {

    private Long id;

    private String tag;

    private String tagType;

    private String title;

    private String summary;

    private String publishDate;

    private boolean pinned;

    private String path;

    private boolean enabled;
}
