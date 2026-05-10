package com.autoapply.service.resume;

import com.autoapply.entity.Resume;
import com.autoapply.repository.ResumeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumePdfParserService {

    private final ResumeRepository resumeRepository;
    private final ResumeJsonExtractor extractor;
    private final ObjectMapper objectMapper;

    @Async
    @Transactional
    public void parseAsync(UUID resumeId, byte[] pdfBytes) {
        try {
            String rawText = extractText(pdfBytes);
            ResumeJson resumeJson = extractor.extract(rawText);
            String json = objectMapper.writeValueAsString(resumeJson);

            Resume resume = resumeRepository.findById(resumeId)
                    .orElseThrow(() -> new IllegalStateException("Resume not found: " + resumeId));
            resume.setParsedJson(json);
            resume.setParseStatus("PARSED");
            resumeRepository.save(resume);
        } catch (Exception ex) {
            log.error("Failed to parse resume {}", resumeId, ex);
            resumeRepository.findById(resumeId).ifPresent(resume -> {
                resume.setParseStatus("FAILED");
                resumeRepository.save(resume);
            });
        }
    }

    private String extractText(byte[] pdfBytes) {
        ByteArrayResource resource = new ByteArrayResource(pdfBytes) {
            @Override
            public String getFilename() { return "resume.pdf"; }
        };
        PagePdfDocumentReader reader = new PagePdfDocumentReader(resource,
                PdfDocumentReaderConfig.builder().build());
        List<Document> docs = reader.get();
        return docs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));
    }
}
