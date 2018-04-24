package com.jscherrer.personal.testhelpers;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.RequestSpotInstancesRequest;
import com.jscherrer.personal.aws.SpotInstanceRequester;
import com.jscherrer.personal.setup.SecurityGroupCreator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class BaseAwsTester {

    private static final Logger LOG = LoggerFactory.getLogger(BaseAwsTester.class);

    protected static final SpotInstanceRequester spotInstanceRequester = new SpotInstanceRequester();
    protected static final SecurityGroupCreator securityGroupCreator = new SecurityGroupCreator();
    protected static final AmazonEC2 EC2 = AmazonEC2ClientBuilder.defaultClient();
    protected static ArrayList<String> requestIds;
    protected static ArrayList<String> instanceIds;

    @BeforeClass
    public static void setUp() {
        requestIds = new ArrayList<>();
        instanceIds = new ArrayList<>();
    }

    @AfterClass
    public static void tearDown() {
        if (requestIds != null && requestIds.size() > 0) {
            spotInstanceRequester.stopSpotRequest(requestIds);
        }
        if (instanceIds != null && instanceIds.size() > 0) {
            spotInstanceRequester.stopInstances(instanceIds);
        }
    }

    protected static void setupSecurityGroup() {
        LOG.info("Setting up AWS test security group");
        securityGroupCreator.createSecurityGroup(AWSTestConstants.SECURITY_GROUP_NAME,
                "Personal Project Aws Test Security Group");
    }

    protected static void startUpSpotInstance() {
        LOG.info("Starting up spot instances");
        RequestSpotInstancesRequest spotInstanceRequest = new RequestSpotInstancesRequest();
        spotInstanceRequest.setSpotPrice(AWSTestConstants.AWS_SPOT_PRICE);
        spotInstanceRequest.setInstanceCount(1);

        requestIds = spotInstanceRequester.makeSpotRequest(spotInstanceRequest);
        spotInstanceRequester.waitForActiveInstances(requestIds);

        instanceIds = spotInstanceRequester.getInstanceIds(requestIds);
    }

}
