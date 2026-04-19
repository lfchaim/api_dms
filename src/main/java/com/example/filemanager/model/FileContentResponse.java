package com.example.filemanager.model;

public class FileContentResponse {

    private String subdirectory;
    private String fileName;
    private String content;

    public FileContentResponse() {
    }

    public FileContentResponse(String subdirectory, String fileName, String content) {
        this.subdirectory = subdirectory;
        this.fileName = fileName;
        this.content = content;
    }

    public String getSubdirectory() {
        return subdirectory;
    }

    public void setSubdirectory(String subdirectory) {
        this.subdirectory = subdirectory;
    }

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
