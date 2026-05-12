package com.yt.evaluation_system.user.controller;

import com.yt.evaluation_system.common.result.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UploadController {

    @PostMapping("/upload")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file.isEmpty()) {
            return Result.fail("请选择图片");
        }
        
        try {
            // 获取项目运行根目录下的 uploads 文件夹
            String projectPath = System.getProperty("user.dir");
            File uploadDir = new File(projectPath, "uploads");
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String ext = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                    : ".jpg";
            String newFilename = UUID.randomUUID().toString() + ext;

            File targetFile = new File(uploadDir, newFilename);
            file.transferTo(targetFile);

            // 构建通过网关可访问的 URL
            // 网关地址为 localhost:8080，路由前缀 /api/user
            String url = "http://localhost:8080/api/user/uploads/" + newFilename;
            
            return Result.ok(url);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.fail("上传失败: " + e.getMessage());
        }
    }
}
