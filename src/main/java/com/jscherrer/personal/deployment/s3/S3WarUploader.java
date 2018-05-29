package com.jscherrer.personal.deployment.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class S3WarUploader {

    private static final Logger LOG = LoggerFactory.getLogger(S3WarUploader.class);
    private AmazonS3 S3 = AmazonS3ClientBuilder.defaultClient();

    public S3WarUploader(AmazonS3 S3) {
        this.S3 = S3;
    }

    public S3WarUploader() {
        this(AmazonS3ClientBuilder.defaultClient());
    }

    public void uploadFileToS3Bucket(String bucketName, String key, File uploadFile) {
        createS3BucketIfNotExisting(bucketName);

        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, uploadFile);
        putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead);
        S3.putObject(putObjectRequest);
        LOG.info("Uploaded " + key + " to " + bucketName);
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
