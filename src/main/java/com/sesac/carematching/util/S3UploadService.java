package com.sesac.carematching.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3UploadService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String saveCommunityImageFile(MultipartFile multipartFile) throws IOException {
        // 원본 파일명
        String originalFilename = multipartFile.getOriginalFilename();
        // UUID와 원본 파일명을 조합하고 "community_image/" 폴더를 prefix로 사용
        String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFilename;
        String fileKey = "community_image/" + uniqueFileName;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());

        // S3에 객체 업로드
        amazonS3.putObject(bucket, fileKey, multipartFile.getInputStream(), metadata);

        // 업로드된 이미지의 URL 반환
        return amazonS3.getUrl(bucket, fileKey).toString();
    }

    // 파일 삭제: URL에서 객체 key를 추출하여 삭제
    public void deleteCommunityImageFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            URL url = new URL(fileUrl);
            // URL 경로에서 선행 "/"를 제거하고 URL 디코딩
            String key = URLDecoder.decode(url.getPath().substring(1), StandardCharsets.UTF_8);
            amazonS3.deleteObject(bucket, key);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public String saveProfileImageFile(MultipartFile multipartFile) throws IOException {
        // 원본 파일명
        String originalFilename = multipartFile.getOriginalFilename();
        // UUID와 원본 파일명을 조합하고 "community_image/" 폴더를 prefix로 사용
        String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFilename;
        String fileKey = "user_profile_image/" + uniqueFileName;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());

        // S3에 객체 업로드
        amazonS3.putObject(bucket, fileKey, multipartFile.getInputStream(), metadata);

        // 업로드된 이미지의 URL 반환
        return amazonS3.getUrl(bucket, fileKey).toString();
    }
}
