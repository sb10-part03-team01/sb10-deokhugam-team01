package com.team01.deokhugam.book.storage;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface ThumbnailStorage {
  String upload(MultipartFile image) throws IOException;

  void delete(String s3Url);
}
