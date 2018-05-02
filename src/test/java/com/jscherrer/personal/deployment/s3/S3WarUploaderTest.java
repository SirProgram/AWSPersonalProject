package com.jscherrer.personal.deployment.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class S3WarUploaderTest {

    private static final Logger LOG = LoggerFactory.getLogger(S3WarUploaderTest.class);
    private static final AmazonS3 S3 = AmazonS3ClientBuilder.defaultClient();
    private static final String uploadFilePath = "src/test/resources/TestFileToUpload.txt";

    private S3WarUploader s3WarUploader = new S3WarUploader();
    private String bucketName;

    @Before
    public void setUp() {
        bucketName = UUID.randomUUID().toString();
    }

    @After
    public void tearDown() {
        if (bucketName != null) {
            cleanUpExistingBucket(bucketName);
        }
    }

    @Test
    public void canUploadFileToS3() {
        File fileToUpload = new File(uploadFilePath);
        String uploadKey = "uploadedFilePathKey";

        s3WarUploader.uploadFileToS3Bucket(bucketName, uploadKey, fileToUpload);

        ObjectListing uploadedObjectListing = getBucketMatchingName(bucketName);
        Assertions.assertThat(uploadedObjectListing.getBucketName()).isEqualTo(bucketName);
        List<S3ObjectSummary> objectSummaries = uploadedObjectListing.getObjectSummaries();
        Assertions.assertThat(objectSummaries).hasSize(1);
        Assertions.assertThat(objectSummaries.get(0).getKey()).isEqualTo(uploadKey);
    }

    @Test
    public void createsS3BucketIfNotExisting() {
        s3WarUploader.createS3BucketIfNotExisting(bucketName);

        ObjectListing bucket = getBucketMatchingName(bucketName);
        Assertions.assertThat(bucket.getBucketName()).isEqualTo(bucketName);
    }

    @Test
    public void doesNotCreateS3BucketIfExisting() {
        //given
        AmazonS3 spyS3 = Mockito.spy(S3);
        S3WarUploader s3WarUploader = new S3WarUploader(spyS3);
        s3WarUploader.createS3BucketIfNotExisting(bucketName);
        Mockito.reset(spyS3);

        //when
        s3WarUploader.createS3BucketIfNotExisting(bucketName);

        //then
        Mockito.verify(spyS3, Mockito.times(0)).createBucket(Mockito.anyString());
        Mockito.verify(spyS3, Mockito.times(0)).createBucket(Mockito.any(CreateBucketRequest.class));
    }

    private ObjectListing getBucketMatchingName(String name) {
        return S3.listObjects(name);
    }

    private void cleanUpExistingBucket(String bucketName) {
        try {
            ObjectListing bucketListing = S3.listObjects(bucketName);
            for (S3ObjectSummary s3ObjectSummary : bucketListing.getObjectSummaries()) {
                S3.deleteObject(bucketName, s3ObjectSummary.getKey());
            }
            S3.deleteBucket(bucketName);
        } catch (AmazonS3Exception e) {
            LOG.warn("Deleting bucket failed, already existing? ", e);
        }
    }
}