package com.team01.deokhugam.book.storage;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@ConditionalOnProperty(name = "deokhugam.storage.type", havingValue = "local", matchIfMissing = true)
@Component
public class LocalThumbnailStorage implements ThumbnailStorage {

  @Value("${deokhugam.storage.local.root-path}")
  private String uploadDir;

  @PostConstruct
  public void init() {
    try {
      Files.createDirectories(Paths.get(uploadDir));
    } catch (IOException e) {
      throw new RuntimeException("로컬 스토리지 디렉토리를 생성할 수 없습니다.", e);
    }
  }

  @Override
  public String upload(MultipartFile image) throws IOException {
    String originalFilename = image.getOriginalFilename();
    String extension = "";
    if (originalFilename != null && originalFilename.contains(".")) {
      extension = originalFilename.substring(originalFilename.lastIndexOf("."));
    }

    String key = UUID.randomUUID().toString() + extension;
    Path targetLocation = Paths.get(uploadDir).resolve(key);

    Files.copy(image.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

    return key;
  }

  @Override
  public void delete(String key) {
    if (key == null || key.isBlank()) return;
    try {
      Path targetLocation = Paths.get(uploadDir).resolve(key);
      Files.deleteIfExists(targetLocation);
    } catch (IOException e) {
      log.error("로컬 파일 삭제 실패: {}", key, e);
    }
  }

  @Override
  public String generatePresignUrl(String key) {
    if (key == null || key.isBlank()) return null;

    return "/images/thumbnails/" + key;
  }
}
