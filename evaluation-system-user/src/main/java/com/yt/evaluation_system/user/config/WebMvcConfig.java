package com.yt.evaluation_system.user.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取当前项目的根目录下的 uploads 文件夹
        String path = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;
        registry.addResourceHandler("/user/uploads/**")
                .addResourceLocations("file:" + path);
    }
}
