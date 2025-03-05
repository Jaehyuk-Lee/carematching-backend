package com.sesac.carematching.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class EnvProperties {
    @Value("${cloud.aws.s3.bucket}")
    private String s3BucketName;

    private static String s3BucketNameStatic;

    @PostConstruct
    public void init() {
        s3BucketNameStatic = s3BucketName;
    }

    public static String getS3BucketName() {
        return s3BucketNameStatic;
    }
}
