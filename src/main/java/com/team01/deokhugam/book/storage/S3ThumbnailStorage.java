package com.team01.deokhugam.book.storage;

import com.team01.deokhugam.global.exception.DeokhugamException;
import com.team01.deokhugam.global.exception.ErrorCode;
import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@ConditionalOnProperty(name = "deokhugam.storage.type", havingValue = "s3",matchIfMissing = true)
@Component
public class S3ThumbnailStorage implements ThumbnailStorage{
  private final S3Client s3Client;
  private final S3Presigner s3Presigner;
  private final String bucket;
  private final int expireMinutes;

  public S3ThumbnailStorage(
      @Value("${deokhugam.storage.s3.access-key}") String accessKey,
      @Value("${deokhugam.storage.s3.secret-key}") String secretKey,
      @Value("${deokhugam.storage.s3.region}") String region,
      @Value("${deokhugam.storage.s3.bucket}") String bucket,
      @Value("${deokhugam.storage.s3.presigned-url-expiration}") int expireMinutes
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

    this.s3Presigner = S3Presigner.builder()
        .region(Region.of(region))
        .credentialsProvider(credentialsProvider)
        .build();

    this.expireMinutes = expireMinutes;
  }

  @Override
  public String upload(MultipartFile image) throws IOException {
    String contentType = image.getContentType();
    if (!StringUtils.hasText(contentType) || !contentType.startsWith("image/")) {
        throw new DeokhugamException(ErrorCode.INVALID_FILE);
      }

    String imageOriginalName = image.getOriginalFilename();
    String extension = "";

    if(imageOriginalName != null && imageOriginalName.contains(".")){
      extension = imageOriginalName.substring(imageOriginalName.lastIndexOf("."));
    }
    // 고유한 파일명 생성 = UUID, 확장자
    String key = UUID.randomUUID().toString() + extension;

    PutObjectRequest request = PutObjectRequest.builder()
        .key(key)
        .bucket(bucket)
        .contentType(image.getContentType())
        .build();

    s3Client.putObject(request, RequestBody.fromInputStream(image.getInputStream(), image.getSize()));
    return key;
  }

  @Override
  public void delete(String key) {
    if(!StringUtils.hasText(key)){
      return;
    }

    s3Client.deleteObject(builder -> builder.bucket(bucket).key(key));
  }

  @Override
  public String generatePresignUrl(String key) {
    if (!StringUtils.hasText(key)) {
      return null;
    }

    // 어떤 파일을 가져올지 요청
    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .build();

    // 얼마나 허용할지 시간을 설정
    GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
        // 유효기간
        .signatureDuration(Duration.ofMinutes(expireMinutes))
        .getObjectRequest(getObjectRequest)
        .build();


    // 문자열로 반환
    return s3Presigner.presignGetObject(presignRequest).url().toString();
  }
}
