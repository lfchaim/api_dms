package com.example.filemanager.model;

public class FileMetadataResponse {

    private String relativePath;
    private String subdirectory;
    private String fileName;
    private long sizeInBytes;
    private String lastModified;

    public FileMetadataResponse() {
    }

    public FileMetadataResponse(String relativePath, String subdirectory, String fileName, long sizeInBytes, String lastModified) {
        this.relativePath = relativePath;
        this.subdirectory = subdirectory;
        this.fileName = fileName;
        this.sizeInBytes = sizeInBytes;
        this.lastModified = lastModified;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
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

    public long getSizeInBytes() {
        return sizeInBytes;
    }

    public void setSizeInBytes(long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }
}
