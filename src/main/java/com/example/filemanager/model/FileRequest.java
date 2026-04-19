package com.example.filemanager.model;

import jakarta.validation.constraints.NotBlank;

public class FileRequest {

    @NotBlank(message = "fileName is required")
    private String fileName;

    @NotBlank(message = "content is required")
    private String content;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
