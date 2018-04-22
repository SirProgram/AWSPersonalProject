package com.jscherrer.personal;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SpotInstanceRequests {

    private static final String AMI = "ami-3bfab942";
    private static final String INSTANCE_TYPE = "t1.micro";
    private static final AmazonEC2 EC2 = AmazonEC2ClientBuilder.defaultClient();

    public ArrayList<String> makeSpotRequest(RequestSpotInstancesRequest spotInstanceRequest) {
        if (spotInstanceRequest.getLaunchSpecification() == null) {
            spotInstanceRequest.setLaunchSpecification(getDefaultLaunchSpecification());
        }

        RequestSpotInstancesResult requestResult = EC2.requestSpotInstances(spotInstanceRequest);
        return spotInstanceRequestIdsFromResponses(requestResult.getSpotInstanceRequests());
    }

    public void stopSpotRequest(List<String> requestIdsToCancel) {
        try {
            CancelSpotInstanceRequestsRequest cancelRequest =
                    new CancelSpotInstanceRequestsRequest(requestIdsToCancel);
            EC2.cancelSpotInstanceRequests(cancelRequest);
        } catch (AmazonServiceException e) {
            System.err.println("Error cancelling instances");
            System.err.println("Caught Exception: " + e.getMessage());
            System.err.println("Reponse Status Code: " + e.getStatusCode());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("Request ID: " + e.getRequestId());
        }
    }

    public void stopInstances(List<String> instanceIdsToTerminate) {
        try {
            TerminateInstancesRequest terminateRequest = new TerminateInstancesRequest(instanceIdsToTerminate);
            EC2.terminateInstances(terminateRequest);
        } catch (AmazonServiceException e) {
            System.err.println("Error terminating instances");
            System.err.println("Caught Exception: " + e.getMessage());
            System.err.println("Reponse Status Code: " + e.getStatusCode());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("Request ID: " + e.getRequestId());
        }
    }

    public void waitForActiveInstances(ArrayList<String> requestIds) {
        boolean waitingOnInstances = true;

        while (waitingOnInstances) {
            DescribeSpotInstanceRequestsRequest describeRequest = new DescribeSpotInstanceRequestsRequest();
            describeRequest.setSpotInstanceRequestIds(requestIds);

            try {
                DescribeSpotInstanceRequestsResult describeResult = EC2.describeSpotInstanceRequests(describeRequest);
                List<SpotInstanceRequest> describeResponses = describeResult.getSpotInstanceRequests();

                waitingOnInstances = false;

                for (SpotInstanceRequest describeResponse : describeResponses) {
                    if (describeResponse.getState().equals("open")) {
                        waitingOnInstances = true;
                        break;
                    }
                }
            } catch (AmazonServiceException e) {
                System.err.println(e.getMessage());
                waitingOnInstances = true;
            }

            try {
                System.out.println("Waiting on instances...");
                Thread.sleep(10*1000);
            } catch (InterruptedException e) {
                //Deliberately do nothing
            }
        }

        System.out.println("All instances active!");
    }

    public ArrayList<String> getInstanceIds(Collection<String> requestIds) {
        DescribeSpotInstanceRequestsRequest describeRequest = new DescribeSpotInstanceRequestsRequest();
        describeRequest.setSpotInstanceRequestIds(requestIds);

        DescribeSpotInstanceRequestsResult describeResult = EC2.describeSpotInstanceRequests(describeRequest);
        ArrayList<String> instanceIds = new ArrayList<>();

        for (SpotInstanceRequest spotInstanceRequest : describeResult.getSpotInstanceRequests()) {
            instanceIds.add(spotInstanceRequest.getInstanceId());
        }
        return instanceIds;
    }

    private ArrayList<String> spotInstanceRequestIdsFromResponses(List<SpotInstanceRequest> requestResponses) {
        ArrayList<String> spotInstanceRequestIds = new ArrayList<>();

        for (SpotInstanceRequest requestResponse : requestResponses) {
            System.out.println("Created Spot Request: " + requestResponse.getSpotInstanceRequestId());
            spotInstanceRequestIds.add(requestResponse.getSpotInstanceRequestId());
        }

        return spotInstanceRequestIds;
    }

    private LaunchSpecification getDefaultLaunchSpecification() {
        LaunchSpecification launchSpecification = new LaunchSpecification();
        launchSpecification.setImageId(AMI);
        launchSpecification.setInstanceType(INSTANCE_TYPE);

        ArrayList<String> securityGroups = new ArrayList<>();
        securityGroups.add(AWSConstants.SECURITY_GROUP_NAME);
        launchSpecification.setSecurityGroups(securityGroups);
        return launchSpecification;
    }
}
