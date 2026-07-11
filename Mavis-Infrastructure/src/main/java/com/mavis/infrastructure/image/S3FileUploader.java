package com.mavis.infrastructure.image;

import com.mavis.common.exception.GlobalErrorCode;
import com.mavis.common.exception.MavisCodeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3FileUploader {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public String uploadImageToS3(MultipartFile file, ImageDirectory directory) {
        String originalFilename = file.getOriginalFilename();
        String extension = Objects.requireNonNull(originalFilename).substring(originalFilename.lastIndexOf(".") + 1);
        String s3FileName = "original/" + directory.getPath() + "/" + UUID.randomUUID().toString().substring(0, 10) + "_" + originalFilename;

        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3FileName)
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .contentType("image/" + extension)
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
        } catch (Exception e) {
            log.error("S3 파일 업로드 실패: {}", s3FileName, e);
            throw new MavisCodeException(GlobalErrorCode.INTERNAL_SERVER_ERROR);
        }

        return s3Client.utilities().getUrl(url -> url.bucket(bucketName).key(s3FileName)).toString();
    }
}
