package com.autoapply.service.resume;

import com.autoapply.entity.Resume;
import com.autoapply.entity.User;
import com.autoapply.exception.ResourceNotFoundException;
import com.autoapply.exception.StorageException;
import com.autoapply.repository.ResumeRepository;
import com.autoapply.repository.UserRepository;
import com.autoapply.service.resume.storage.StorageResult;
import com.autoapply.service.resume.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private static final long MAX_FILE_BYTES = 10 * 1024 * 1024L; // 10 MB
    private static final String PDF_CONTENT_TYPE = "application/pdf";

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final ResumePdfParserService parserService;

    @Transactional
    public Resume upload(MultipartFile file, UUID userId) {
        validateFile(file);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException ex) {
            throw new StorageException("Failed to read uploaded file", ex);
        }

        String path = userId + "/" + UUID.randomUUID() + ".pdf";
        StorageResult result = storageService.upload(bytes, path, PDF_CONTENT_TYPE);

        Resume resume = resumeRepository.save(Resume.builder()
                .user(user)
                .originalName(file.getOriginalFilename())
                .storagePath(result.storagePath())
                .storageUrl(result.publicUrl())
                .fileSize(file.getSize())
                .parseStatus("PENDING")
                .build());

        // Async parse — separate bean required for proxy to work
        parserService.parseAsync(resume.getId(), bytes);

        return resume;
    }

    public List<Resume> listForUser(UUID userId) {
        return resumeRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Resume getForUser(UUID id, UUID userId) {
        return resumeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
    }

    @Transactional
    public void deleteForUser(UUID id, UUID userId) {
        Resume resume = resumeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
        storageService.delete(resume.getStoragePath());
        resumeRepository.delete(resume);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file provided");
        }
        String contentType = file.getContentType();
        if (!PDF_CONTENT_TYPE.equals(contentType)) {
            throw new IllegalArgumentException("Only PDF files are supported");
        }
        if (file.getSize() > MAX_FILE_BYTES) {
            throw new IllegalArgumentException("File size exceeds 10 MB limit");
        }
    }
}
