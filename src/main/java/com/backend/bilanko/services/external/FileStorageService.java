package com.backend.bilanko.services.external;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String uploadProfilePicture(MultipartFile file, long userId);
}
