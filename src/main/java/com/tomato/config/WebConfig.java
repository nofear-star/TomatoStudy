package com.tomato.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:./uploads/avatars}")
    private String uploadPath;

    @Value("${file.upload.url-prefix:/api/uploads/avatars}")
    private String urlPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 静态资源
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
        
        // 上传的文件资源
        // 注意：由于 context-path 是 /api，所以资源处理器路径不需要 /api 前缀
        // 如果 urlPrefix 是 /api/uploads/avatars，资源处理器应该是 /uploads/avatars/**
        // 这样实际访问路径是 /api/uploads/avatars/**
        String resourceHandler = urlPrefix.startsWith("/api/") 
            ? urlPrefix.substring(4)  // 去掉 /api 前缀
            : urlPrefix;
        
        String fileUrl = "file:" + Paths.get(uploadPath).toAbsolutePath().normalize().toString().replace("\\", "/") + "/";
        System.out.println("配置静态资源访问:");
        System.out.println("  资源处理器: " + resourceHandler + "/**");
        System.out.println("  文件路径: " + fileUrl);
        System.out.println("  实际访问路径: /api" + resourceHandler + "/**");
        
        registry.addResourceHandler(resourceHandler + "/**")
                .addResourceLocations(fileUrl)
                .setCachePeriod(0); // 不缓存，方便调试
    }
    
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseTrailingSlashMatch(false);
    }
}

