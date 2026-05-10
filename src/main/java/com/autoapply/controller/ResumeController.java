package com.autoapply.controller;

import com.autoapply.dto.response.ResumeResponse;
import com.autoapply.entity.Resume;
import com.autoapply.service.auth.UserPrincipal;
import com.autoapply.service.resume.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResumeResponse> upload(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal) {
        Resume resume = resumeService.upload(file, principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(resume));
    }

    @GetMapping
    public ResponseEntity<List<ResumeResponse>> list(@AuthenticationPrincipal UserPrincipal principal) {
        List<ResumeResponse> responses = resumeService.listForUser(principal.getUserId())
                .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeResponse> get(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(toResponse(resumeService.getForUser(id, principal.getUserId())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        resumeService.deleteForUser(id, principal.getUserId());
        return ResponseEntity.noContent().build();
    }

    private ResumeResponse toResponse(Resume resume) {
        return ResumeResponse.builder()
                .id(resume.getId())
                .originalName(resume.getOriginalName())
                .storageUrl(resume.getStorageUrl())
                .parseStatus(resume.getParseStatus())
                .fileSize(resume.getFileSize())
                .parsedJson(resume.getParsedJson())
                .createdAt(resume.getCreatedAt())
                .build();
    }
}
