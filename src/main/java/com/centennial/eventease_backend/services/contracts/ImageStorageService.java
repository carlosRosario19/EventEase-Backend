package com.centennial.eventease_backend.services.contracts;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {
    void init();
    String store(MultipartFile file);
    Resource load(String filename);
}
