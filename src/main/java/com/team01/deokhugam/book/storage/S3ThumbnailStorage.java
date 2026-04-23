package com.team01.deokhugam.book.storage;

import java.io.IOException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@ConditionalOnProperty(name = "deokhugam.storage.type", havingValue = "s3")
@Component
public class S3ThumbnailStorage implements ThumbnailStorage{
  private final S3Client s3Client;
  private final String bucket;

  public S3ThumbnailStorage(
      @Value("${deokhugam.storage.s3.access-key}") String accessKey,
      @Value("${deokhugam.storage.s3.secret-key}") String secretKey,
      @Value("${deokhugam.storage.s3.region}") String region,
      @Value("${deokhugam.storage.s3.bucket}") String bucket
  ){
    this.bucket = bucket;
    // 공통 자격 증명
    StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
        AwsBasicCredentials.create(accessKey, secretKey)
    );

    this.s3Client = S3Client.builder()
        .region(Region.of(region))
        .credentialsProvider(credentialsProvider)
        .build();
  }

  @Override
  public String upload(MultipartFile image) throws IOException {
    String imageOriginalName = image.getOriginalFilename();
    String extension = "";

    if(imageOriginalName != null && imageOriginalName.contains(".")){
      extension = imageOriginalName.substring(imageOriginalName.indexOf("."));
    }
    // 고유한 파일명 생성 = UUID, 확장자
    String key = UUID.randomUUID().toString() + extension;

    PutObjectRequest request = PutObjectRequest.builder()
        .key(key)
        .bucket(bucket)
        .contentType(image.getContentType())
        .build();

    s3Client.putObject(request, RequestBody.fromBytes(image.getBytes()));
    return s3Client.utilities().getUrl(builder -> builder.bucket(bucket).key(key)).toString();
  }

  @Override
  public void delete(String s3Url) {
    if(s3Url == null || !s3Url.contains(".amazonaws.com/")){
      return;
    }

    String key = s3Url.substring(s3Url.indexOf(".amazonaws.com/") + 15);

    s3Client.deleteObject(builder -> builder.bucket(bucket).key(key));
  }
}
