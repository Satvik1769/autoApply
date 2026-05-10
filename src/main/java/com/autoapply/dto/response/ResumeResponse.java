package com.autoapply.dto.response;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeResponse {
    private UUID id;
    private String originalName;
    private String storageUrl;
    private String parseStatus;
    private Long fileSize;

    @JsonRawValue
    private String parsedJson;

    private OffsetDateTime createdAt;
}
