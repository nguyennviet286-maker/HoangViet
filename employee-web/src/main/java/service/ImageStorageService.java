package com.example.employeeweb.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageStorageService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp"
    );

    private final Path uploadDirectory;

    public ImageStorageService(
            @Value("${app.upload.dir:uploads/employee-images}") String uploadDir) {
        this.uploadDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public String store(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Image must be 5 MB or smaller.");
        }

        String extension = EXTENSIONS.get(file.getContentType());
        if (extension == null) {
            throw new IllegalArgumentException("Only JPEG, PNG, and WebP images are allowed.");
        }

        Files.createDirectories(uploadDirectory);

        String fileName = UUID.randomUUID() + extension;
        Path destination = uploadDirectory.resolve(fileName).normalize();

        if (!destination.startsWith(uploadDirectory)) {
            throw new IllegalArgumentException("Invalid image path.");
        }

        file.transferTo(destination);
        return "/uploads/" + fileName;
    }

    public Path getUploadDirectory() {
        return uploadDirectory;
    }
}