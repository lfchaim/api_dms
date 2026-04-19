package com.example.filemanager.controller;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.filemanager.model.ApiMessageResponse;
import com.example.filemanager.model.FileContentResponse;
import com.example.filemanager.model.FileMetadataResponse;
import com.example.filemanager.model.FileRequest;
import com.example.filemanager.service.FileManagerService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@Validated
@RestController
@RequestMapping("/api/files")
public class FileManagerController {

    private final FileManagerService fileManagerService;

    public FileManagerController(FileManagerService fileManagerService) {
        this.fileManagerService = fileManagerService;
    }

    @PostMapping("/{subdirectory}")
    public ResponseEntity<ApiMessageResponse> createFile(
            @PathVariable String subdirectory,
            @Valid @RequestBody FileRequest request
    ) {
        fileManagerService.createFile(subdirectory, request.getFileName(), request.getContent());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiMessageResponse("File created successfully"));
    }

    @PutMapping("/{subdirectory}")
    public ResponseEntity<ApiMessageResponse> updateFile(
            @PathVariable String subdirectory,
            @Valid @RequestBody FileRequest request
    ) {
        fileManagerService.updateFile(subdirectory, request.getFileName(), request.getContent());
        return ResponseEntity.ok(new ApiMessageResponse("File updated successfully"));
    }

    @DeleteMapping("/{subdirectory}")
    public ResponseEntity<ApiMessageResponse> deleteFile(
            @PathVariable String subdirectory,
            @RequestParam(name = "file_name") @NotBlank String fileName
    ) {
        fileManagerService.deleteFile(subdirectory, fileName);
        return ResponseEntity.ok(new ApiMessageResponse("File deleted successfully"));
    }

    @GetMapping
    public ResponseEntity<List<FileMetadataResponse>> listFiles() {
        return ResponseEntity.ok(fileManagerService.listAllFiles());
    }

    @GetMapping("/{subdirectory}")
    public ResponseEntity<FileContentResponse> getFileContent(
            @PathVariable String subdirectory,
            @RequestParam(name = "file_name") @NotBlank String fileName
    ) {
    	System.out.println("subdirectory: "+subdirectory+" file_name: "+fileName);
        return ResponseEntity.ok(fileManagerService.getFileContent(subdirectory, fileName));
    }
    
    @GetMapping("/download/{subdirectory}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String subdirectory,
            @RequestParam(name = "file_name") @NotBlank String fileName
    ) throws IOException {
    	Path path = fileManagerService.getFilePath(subdirectory, fileName);
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
            .body(resource);
    }
    
}
