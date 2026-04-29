package com.team01.deokhugam.book.storage;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

class LocalThumbnailStorageTest {

  private LocalThumbnailStorage localStorage;

  @TempDir
  Path tempDir; // 테스트용 임시 디렉토리 (테스트 종료 시 자동 삭제됨)

  @BeforeEach
  void setUp() {
    localStorage = new LocalThumbnailStorage();
    // @Value 로 주입받는 필드를 ReflectionTestUtils로 강제 주입
    ReflectionTestUtils.setField(localStorage, "uploadDir", tempDir.toString());
    localStorage.init(); // 디렉토리 생성
  }

  @Test
  @DisplayName("로컬에 이미지를 정상적으로 업로드하고 파일명을 반환한다")
  void upload_success() throws IOException {
    // given
    MockMultipartFile file = new MockMultipartFile(
        "image",
        "test.jpg",
        "image/jpeg",
        "dummy image content".getBytes()
    );

    // when
    String key = localStorage.upload(file);

    // then
    assertThat(key).isNotNull();
    assertThat(key).endsWith(".jpg");

    // 실제로 임시 폴더에 파일이 만들어졌는지 검증
    Path savedFilePath = tempDir.resolve(key);
    assertThat(Files.exists(savedFilePath)).isTrue();
  }

  @Test
  @DisplayName("파일 키로 로컬 이미지를 삭제한다")
  void delete_success() throws IOException {
    // given
    String testKey = "delete-test.png";
    Path testFile = tempDir.resolve(testKey);
    Files.createFile(testFile); // 가짜 파일 미리 생성

    // when
    localStorage.delete(testKey);

    // then
    assertThat(Files.exists(testFile)).isFalse(); // 파일이 지워졌는지 검증
  }

  @Test
  @DisplayName("파일 키로 Presigned URL 형식의 문자열을 반환한다")
  void generatePresignUrl_success() {
    // given
    String testKey = "my-image.jpg";

    // when
    String result = localStorage.generatePresignUrl(testKey);

    // then
    assertThat(result).isEqualTo("/qa-images/my-image.jpg");
  }
}
