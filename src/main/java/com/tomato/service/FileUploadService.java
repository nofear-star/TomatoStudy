package com.tomato.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileUploadService {

    @Value("${file.upload.path:./uploads/avatars}")
    private String uploadPath;

    @Value("${file.upload.url-prefix:/api/uploads/avatars}")
    private String urlPrefix;

    /**
     * 上传头像文件
     * @param file 上传的文件
     * @param userId 用户ID
     * @return 文件的访问URL
     * @throws IOException 文件操作异常
     */
    public String uploadAvatar(MultipartFile file, Long userId) throws IOException {
        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("只能上传图片文件");
        }

        // 验证文件大小（5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("图片大小不能超过5MB");
        }

        // 创建上传目录（如果不存在）
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // 生成唯一文件名：userId_时间戳_随机UUID.扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = userId + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;

        // 保存文件
        Path filePath = uploadDir.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // 返回访问URL
        return urlPrefix + "/" + filename;
    }

    /**
     * 删除旧头像文件
     * @param avatarUrl 头像URL
     */
    public void deleteAvatar(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            return;
        }

        try {
            // 从URL中提取文件名
            String filename = avatarUrl.substring(avatarUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get(uploadPath, filename);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            // 删除失败不影响主流程，只记录日志
            System.err.println("删除旧头像文件失败: " + avatarUrl + ", 错误: " + e.getMessage());
        }
    }
}

