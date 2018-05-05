package com.jscherrer.personal.deployment.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CreateBucketRequest;

import java.io.File;

public class S3WarUploader {

    private AmazonS3 S3 = AmazonS3ClientBuilder.defaultClient();

    public S3WarUploader(AmazonS3 S3) {
        this.S3 = S3;
    }

    public S3WarUploader() {
        this(AmazonS3ClientBuilder.defaultClient());
    }

    public void uploadFileToS3Bucket(String bucketName, String key, File uploadFile) {
        createS3BucketIfNotExisting(bucketName);
        S3.putObject(bucketName, key, uploadFile);
    }

    public boolean createS3BucketIfNotExisting(String bucketName) {
        if (!checkBucketExists(bucketName)) {
            CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucketName);
            S3.createBucket(createBucketRequest);
            return true;
        }
        return false;
    }

    private boolean checkBucketExists(String bucketName) {
        try {
            return S3.listObjects(bucketName).getBucketName().equals(bucketName);
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() == 404 && e.getErrorCode().equals("NoSuchBucket")) {
                return false;
            }
            throw e;
        }
    }
}
