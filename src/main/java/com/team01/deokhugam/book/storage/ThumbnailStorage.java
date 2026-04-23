package com.team01.deokhugam.book.storage;

import java.io.IOException;
import org.mapstruct.Named;
import org.springframework.web.multipart.MultipartFile;

public interface ThumbnailStorage {
  String upload(MultipartFile image) throws IOException;

  void delete(String s3Url);

  // mapstruct가 해당 이름으로 찾을 수 있게함
  @Named("toPresignedUrl")
  String generatePresignUrl(String key);
}
