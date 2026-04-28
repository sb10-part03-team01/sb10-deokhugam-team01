package com.team01.deokhugam.global.config;

import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  @Value("${deokhugam.storage.local.root-path}")
  String uploadDir;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    String location = Paths.get(uploadDir).toAbsolutePath().toUri().toString();
    registry.addResourceHandler("/qa-images/**")
        .addResourceLocations(location);
  }
}
