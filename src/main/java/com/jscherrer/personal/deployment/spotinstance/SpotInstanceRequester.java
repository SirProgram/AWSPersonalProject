package com.jscherrer.personal.deployment.spotinstance;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.jscherrer.personal.deployment.DefaultLaunchConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SpotInstanceRequester {

    private static final Logger LOG = LoggerFactory.getLogger(SpotInstanceRequester.class);
    private static final AmazonEC2 EC2 = AmazonEC2ClientBuilder.defaultClient();

    public ArrayList<String> makeSpotRequest(RequestSpotInstancesRequest spotInstanceRequest, Collection<String> securityGroups) {
        if (spotInstanceRequest.getLaunchSpecification() == null) {
            spotInstanceRequest.setLaunchSpecification(DefaultLaunchConfiguration.getDefaultLaunchSpecification(EC2));
        }
        spotInstanceRequest.getLaunchSpecification().setSecurityGroups(securityGroups);

        return makeSpotRequest(spotInstanceRequest);
    }

    public ArrayList<String> makeSpotRequest(RequestSpotInstancesRequest spotInstanceRequest) {
        if (spotInstanceRequest.getLaunchSpecification() == null) {
            spotInstanceRequest.setLaunchSpecification(DefaultLaunchConfiguration.getDefaultLaunchSpecification(EC2));
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
            LOG.error("Error cancelling instances");
            LOG.error("Caught Exception: ", e);
        }
    }

    public void stopInstances(List<String> instanceIdsToTerminate) {
        try {
            TerminateInstancesRequest terminateRequest = new TerminateInstancesRequest(instanceIdsToTerminate);
            EC2.terminateInstances(terminateRequest);
        } catch (AmazonServiceException e) {
            LOG.error("Error terminating instances");
            LOG.error("Caught Exception: ", e);
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
                    if (describeResponse.getState().equals(SpotInstanceState.Open.toString())) {
                        waitingOnInstances = true;
                        break;
                    }
                }
            } catch (AmazonServiceException e) {
                LOG.error(e.getMessage());
                waitingOnInstances = true;
            }

            try {
                LOG.info("Waiting on instances...");
                Thread.sleep(10*1000);
            } catch (InterruptedException e) {
                //Deliberately do nothing
            }
        }

        LOG.info("All instances active!");
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

    public List<Instance> describeInstances(Collection<String> instanceIds) {
        DescribeInstancesRequest describeRequest = new DescribeInstancesRequest();
        describeRequest.setInstanceIds(instanceIds);

        DescribeInstancesResult describeResult = EC2.describeInstances(describeRequest);
        return describeResult.getReservations().stream()
                .flatMap(r -> r.getInstances().stream())
                .collect(Collectors.toList());
    }

    private ArrayList<String> spotInstanceRequestIdsFromResponses(List<SpotInstanceRequest> requestResponses) {
        ArrayList<String> spotInstanceRequestIds = new ArrayList<>();

        for (SpotInstanceRequest requestResponse : requestResponses) {
            LOG.info("Created Spot Request: " + requestResponse.getSpotInstanceRequestId());
            spotInstanceRequestIds.add(requestResponse.getSpotInstanceRequestId());
        }

        return spotInstanceRequestIds;
    }

}
