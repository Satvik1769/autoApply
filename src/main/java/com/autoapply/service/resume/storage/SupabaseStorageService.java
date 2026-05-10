package com.autoapply.service.resume.storage;

import com.autoapply.config.AppProperties;
import com.autoapply.exception.StorageException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;


@Service
@RequiredArgsConstructor
public class SupabaseStorageService implements StorageService {

    private final WebClient supabaseStorageClient;
    private final AppProperties appProperties;

    @Override
    public StorageResult upload(byte[] content, String path, String contentType) {
        String bucket = appProperties.getSupabase().getStorage().getBucket();
        try {
            supabaseStorageClient.put()
                    .uri("/storage/v1/object/{bucket}/{path}", bucket, path)
                    .header("x-upsert", "true")
                    .contentType(MediaType.parseMediaType(contentType))
                    .bodyValue(content)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException ex) {
            throw new StorageException("Upload failed [" + ex.getStatusCode() + "]: " + ex.getResponseBodyAsString(), ex);
        }
        return new StorageResult(path, getPublicUrl(path));
    }

    @Override
    public void delete(String path) {
        String bucket = appProperties.getSupabase().getStorage().getBucket();
        try {
            supabaseStorageClient.delete()
                    .uri("/storage/v1/object/{bucket}/{path}", bucket, path)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException ex) {
            throw new StorageException("Delete failed [" + ex.getStatusCode() + "]: " + ex.getResponseBodyAsString(), ex);
        }
    }

    @Override
    public String getPublicUrl(String path) {
        String bucket = appProperties.getSupabase().getStorage().getBucket();
        return appProperties.getSupabase().getUrl()
                + "/storage/v1/object/public/" + bucket + "/" + path;
    }
}
