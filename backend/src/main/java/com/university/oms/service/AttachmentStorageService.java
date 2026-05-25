package com.university.oms.service;

import com.university.oms.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Service
public class AttachmentStorageService {
    private static final long MAX_BYTES = 20L * 1024L * 1024L;
    private static final Set<String> EXTENSIONS = new HashSet<String>(
            Arrays.asList("pdf", "doc", "docx", "jpg", "jpeg", "png"));
    private final Path uploadRoot;

    public AttachmentStorageService(@Value("${oms.upload-dir:${user.home}/.oms/uploads}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public String store(Long id, MultipartFile file) {
        validate(file);
        Path target = uploadRoot.resolve(id + "." + extension(file.getOriginalFilename())).normalize();
        if (!target.startsWith(uploadRoot)) {
            throw new BusinessException("非法文件路径");
        }
        try {
            Files.createDirectories(uploadRoot);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return target.toString();
        } catch (IOException e) {
            throw new BusinessException("材料上传失败");
        }
    }

    public Resource load(String storagePath) {
        if (storagePath == null || storagePath.trim().isEmpty()) {
            throw new BusinessException("材料文件不可用");
        }
        Path path = Paths.get(storagePath).toAbsolutePath().normalize();
        if (!path.startsWith(uploadRoot) || !Files.exists(path)) {
            throw new BusinessException("材料文件不可用");
        }
        return new FileSystemResource(path.toFile());
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择用印材料文件");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BusinessException("单个材料不得超过20 MB");
        }
        if (!EXTENSIONS.contains(extension(file.getOriginalFilename()))) {
            throw new BusinessException("仅支持 PDF、DOC、DOCX、JPG、JPEG、PNG 文件");
        }
    }

    private String extension(String originalName) {
        int split = originalName == null ? -1 : originalName.lastIndexOf('.');
        return split < 0 ? "" : originalName.substring(split + 1).toLowerCase(Locale.ROOT);
    }
}
