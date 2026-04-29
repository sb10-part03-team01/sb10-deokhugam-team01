package com.team01.deokhugam.book.storage;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team01.deokhugam.global.exception.DeokhugamException;
import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@ExtendWith(MockitoExtension.class)
class S3ThumbnailStorageTest {

  private S3ThumbnailStorage s3Storage;

  @Mock
  private S3Client s3Client;

  @Mock
  private S3Presigner s3Presigner;

  @BeforeEach
  void setUp() {
    // 더미 데이터로 객체 생성 (실제 AWS 연결을 피하기 위해 가짜 키 입력)
    s3Storage = new S3ThumbnailStorage(
        "dummyAccessKey", "dummySecretKey", "ap-northeast-2", "my-bucket", 10
    );

    // 생성자 내부에서 만들어진 실제 객체를 Mock 객체로 바꿔치기
    ReflectionTestUtils.setField(s3Storage, "s3Client", s3Client);
    ReflectionTestUtils.setField(s3Storage, "s3Presigner", s3Presigner);
  }

  @Test
  @DisplayName("이미지 파일을 S3에 정상적으로 업로드하고 키를 반환한다")
  void upload_success() throws IOException {
    // given
    MockMultipartFile file = new MockMultipartFile(
        "image", "cover.png", "image/png", "dummy".getBytes()
    );

    // when
    String key = s3Storage.upload(file);

    // then
    assertThat(key).isNotNull();
    assertThat(key).endsWith(".png");

    // s3Client.putObject가 실제로 1번 호출되었는지 검증
    verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  @Test
  @DisplayName("이미지 타입이 아니면 DeokhugamException을 던진다")
  void upload_fail_not_image() {
    // given
    MockMultipartFile file = new MockMultipartFile(
        "file", "text.txt", "text/plain", "dummy".getBytes()
    );

    // when & then
    assertThatThrownBy(() -> s3Storage.upload(file))
        .isInstanceOf(DeokhugamException.class);
  }

  @Test
  @DisplayName("파일 키로 S3 객체 삭제를 요청한다")
  void delete_success() {
    // given
    String testKey = "delete-me.jpg";

    // when
    s3Storage.delete(testKey);

    // then
    verify(s3Client).deleteObject(any(Consumer.class));
  }

  @Test
  @DisplayName("파일 키로 S3 Presigned URL을 생성하여 반환한다")
  void generatePresignUrl_success() throws Exception {
    // given
    String testKey = "my-book.jpg";
    URL mockUrl = new URL("https://my-bucket.s3.amazonaws.com/my-book.jpg?signature=xxx");

    // 깊은 Mocking (Deep Mocking): presignGetObject가 반환할 가짜 응답을 조립
    PresignedGetObjectRequest presignedRequest = mock(PresignedGetObjectRequest.class);
    when(presignedRequest.url()).thenReturn(mockUrl);
    when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presignedRequest);

    // when
    String result = s3Storage.generatePresignUrl(testKey);

    // then
    assertThat(result).isEqualTo(mockUrl.toString());
  }
}
