package com.example.filemanager.service;

import com.example.filemanager.config.FileStorageProperties;
import com.example.filemanager.exception.ConflictException;
import com.example.filemanager.exception.FileStorageException;
import com.example.filemanager.exception.ResourceNotFoundException;
import com.example.filemanager.exception.InvalidRequestException;
import com.example.filemanager.model.FileContentResponse;
import com.example.filemanager.model.FileMetadataResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Service
public class FileManagerService {

    private static final Set<String> ALLOWED_SUBDIRECTORIES = Set.of("dataset", "credential", "permission", "query");

    private final FileStorageProperties fileStorageProperties;
    private Path baseDirectory;

    public FileManagerService(FileStorageProperties fileStorageProperties) {
        this.fileStorageProperties = fileStorageProperties;
    }

    @PostConstruct
    public void initializeDirectories() {
        try {
            this.baseDirectory = Paths.get(fileStorageProperties.getBasePath()).toAbsolutePath().normalize();
            Files.createDirectories(baseDirectory);
            Files.createDirectories(baseDirectory.resolve("dataset"));
            Files.createDirectories(baseDirectory.resolve("credential"));
        } catch (IOException ex) {
            throw new FileStorageException("Failed to initialize base directories", ex);
        }
    }

    public void createFile(String subdirectory, String fileName, String content) {
        Path targetFile = resolveTargetFile(subdirectory, fileName);
        if (Files.exists(targetFile)) {
            throw new ConflictException("File already exists: " + fileName + " in " + subdirectory);
        }

        writeToFile(targetFile, content, StandardOpenOption.CREATE_NEW);
    }

    public void updateFile(String subdirectory, String fileName, String content) {
        Path targetFile = resolveTargetFile(subdirectory, fileName);
        if (!Files.exists(targetFile)) {
            throw new ResourceNotFoundException("File not found: " + fileName + " in " + subdirectory);
        }

        writeToFile(targetFile, content, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public void deleteFile(String subdirectory, String fileName) {
        Path targetFile = resolveTargetFile(subdirectory, fileName);
        if (!Files.exists(targetFile)) {
            throw new ResourceNotFoundException("File not found: " + fileName + " in " + subdirectory);
        }

        try {
            Files.delete(targetFile);
        } catch (IOException ex) {
            throw new FileStorageException("Failed to delete file: " + fileName, ex);
        }
    }

    public List<FileMetadataResponse> listAllFiles() {
        try (Stream<Path> pathStream = Files.walk(baseDirectory)) {
            return pathStream
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(Path::toString))
                    .map(this::toMetadataResponse)
                    .toList();
        } catch (IOException ex) {
            throw new FileStorageException("Failed to list files", ex);
        }
    }

    public Path getFilePath(String subdirectory, String fileName) {
        Path targetFile = resolveTargetFile(subdirectory, fileName);
        return targetFile;
    }
    
    public FileContentResponse getFileContent(String subdirectory, String fileName) {
        Path targetFile = resolveTargetFile(subdirectory, fileName);
        if (!Files.exists(targetFile)) {
            throw new ResourceNotFoundException("File not found: " + fileName + " in " + subdirectory);
        }

        try {
            String content = Files.readString(targetFile, StandardCharsets.UTF_8);
            return new FileContentResponse(subdirectory, fileName, content);
        } catch (IOException ex) {
            throw new FileStorageException("Failed to read file: " + fileName, ex);
        }
    }

    private void writeToFile(Path filePath, String content, StandardOpenOption option) {
        try {
            Files.writeString(filePath, content, StandardCharsets.UTF_8, StandardOpenOption.WRITE, option);
        } catch (IOException ex) {
            throw new FileStorageException("Failed to write file: " + filePath.getFileName(), ex);
        }
    }

    private Path resolveTargetFile(String subdirectory, String fileName) {
        String normalizedSubdirectory = validateSubdirectory(subdirectory);
        String normalizedFileName = sanitizeFileName(fileName);

        Path targetDirectory = baseDirectory.resolve(normalizedSubdirectory).normalize();
        Path targetFile = targetDirectory.resolve(normalizedFileName).normalize();

        if (!targetFile.startsWith(targetDirectory)) {
            throw new InvalidRequestException("Invalid file path");
        }

        return targetFile;
    }

    private String validateSubdirectory(String subdirectory) {
        if (!StringUtils.hasText(subdirectory)) {
            throw new InvalidRequestException("subdirectory is required");
        }

        String normalized = subdirectory.trim().toLowerCase();
        if (!ALLOWED_SUBDIRECTORIES.contains(normalized)) {
            throw new InvalidRequestException("subdirectory must be one of: dataset, credential");
        }
        return normalized;
    }

    private String sanitizeFileName(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            throw new InvalidRequestException("fileName is required");
        }

        String normalizedFileName = fileName.trim();
        if (normalizedFileName.contains("/") || normalizedFileName.contains("\\") || normalizedFileName.contains("..")) {
            throw new InvalidRequestException("Invalid fileName");
        }

        return normalizedFileName;
    }

    private FileMetadataResponse toMetadataResponse(Path filePath) {
        try {
            Path relativePath = baseDirectory.relativize(filePath);
            String subdirectory = relativePath.getNameCount() > 1 ? relativePath.getName(0).toString() : "";
            String fileName = filePath.getFileName().toString();
            long size = Files.size(filePath);
            String lastModified = DateTimeFormatter.ISO_OFFSET_DATE_TIME
                    .format(Files.getLastModifiedTime(filePath).toInstant().atOffset(ZoneOffset.UTC));

            return new FileMetadataResponse(relativePath.toString(), subdirectory, fileName, size, lastModified);
        } catch (IOException ex) {
            throw new FileStorageException("Failed to build metadata for file: " + filePath, ex);
        }
    }
}
