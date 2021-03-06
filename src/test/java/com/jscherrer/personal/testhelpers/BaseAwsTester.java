package com.jscherrer.personal.testhelpers;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.LaunchSpecification;
import com.amazonaws.services.ec2.model.RequestSpotInstancesRequest;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.jscherrer.personal.deployment.DefaultLaunchConfiguration;
import com.jscherrer.personal.deployment.setup.SecurityGroupCreator;
import com.jscherrer.personal.deployment.spotinstance.SpotInstanceRequester;
import org.junit.AfterClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.ArrayList;

public class BaseAwsTester {

    private static final Logger LOG = LoggerFactory.getLogger(BaseAwsTester.class);

    protected static final SpotInstanceRequester spotInstanceRequester = new SpotInstanceRequester();
    protected static final SecurityGroupCreator securityGroupCreator = new SecurityGroupCreator();
    protected static final AmazonEC2 EC2 = AmazonEC2ClientBuilder.defaultClient();
    protected static final AmazonIdentityManagement IAM = AmazonIdentityManagementClientBuilder.defaultClient();
    protected static ArrayList<String> requestIds = new ArrayList<>();
    protected static ArrayList<String> instanceIds = new ArrayList<>();

    @AfterClass
    public static void tearDownClass() {
        if (requestIds != null && requestIds.size() > 0) {
            spotInstanceRequester.stopSpotRequest(requestIds);
        }
        if (instanceIds != null && instanceIds.size() > 0) {
            spotInstanceRequester.stopInstances(instanceIds);
        }
    }

    protected static void setupSecurityGroup() throws UnknownHostException {
        LOG.info("Setting up AWS test security group");
        securityGroupCreator.createSecurityGroup(AWSTestConstants.SECURITY_GROUP_NAME,
                "Personal Project Aws Test Security Group");
    }

    protected static void startUpSpotInstance(LaunchSpecification launchSpecification) {
        LOG.info("Starting up spot instances");
        RequestSpotInstancesRequest spotInstanceRequest = new RequestSpotInstancesRequest();
        spotInstanceRequest.setSpotPrice(AWSTestConstants.AWS_SPOT_PRICE);
        spotInstanceRequest.setInstanceCount(1);
        spotInstanceRequest.setLaunchSpecification(launchSpecification);

        ArrayList<String> testSecurityGroups = new ArrayList<>();
        testSecurityGroups.add(AWSTestConstants.SECURITY_GROUP_NAME);
        requestIds = spotInstanceRequester.makeSpotRequest(spotInstanceRequest, testSecurityGroups);
        spotInstanceRequester.waitForActiveInstances(requestIds);

        instanceIds = spotInstanceRequester.getInstanceIds(requestIds);
    }

}
