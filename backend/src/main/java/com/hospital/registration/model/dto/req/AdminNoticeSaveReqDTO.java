package com.hospital.registration.model.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AdminNoticeSaveReqDTO {

    @NotBlank(message = "tag is required")
    @Size(max = 20, message = "tag length must be <= 20")
    private String tag;

    @NotBlank(message = "tagType is required")
    @Pattern(regexp = "^(?i)(primary|success|warning|info|danger)$",
            message = "tagType must be primary/success/warning/info/danger")
    private String tagType;

    @NotBlank(message = "title is required")
    @Size(max = 120, message = "title length must be <= 120")
    private String title;

    @Size(max = 500, message = "summary length must be <= 500")
    private String summary;

    @NotNull(message = "publishDate is required")
    private LocalDate publishDate;

    @NotNull(message = "pinned is required")
    private Boolean pinned;

    @Size(max = 255, message = "path length must be <= 255")
    private String path;

    @NotNull(message = "enabled is required")
    private Boolean enabled;
}
