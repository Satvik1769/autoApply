package com.autoapply.service.resume.storage;

public interface StorageService {
    StorageResult upload(byte[] content, String path, String contentType);
    void delete(String path);
    String getPublicUrl(String path);
}
