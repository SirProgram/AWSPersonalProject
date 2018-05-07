package com.jscherrer.personal.deployment.spotinstance;

import com.amazonaws.services.ec2.model.RequestSpotInstancesRequest;
import com.jscherrer.personal.testhelpers.AWSTestConstants;
import com.jscherrer.personal.testhelpers.BaseAwsTester;
import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.ArrayList;

public class SpotInstanceRequesterTest extends BaseAwsTester {

    @BeforeClass
    public static void setUp() throws UnknownHostException {
        setupSecurityGroup();
    }

    @Test
    public void instanceRequestCreatesTwoInstances() {
        RequestSpotInstancesRequest spotInstanceRequest = new RequestSpotInstancesRequest();
        spotInstanceRequest.setSpotPrice("0.03");
        int instanceCount = 2;
        spotInstanceRequest.setInstanceCount(instanceCount);

        ArrayList<String> testSecurityGroups = new ArrayList<>();
        testSecurityGroups.add(AWSTestConstants.SECURITY_GROUP_NAME);

        requestIds = spotInstanceRequester.makeSpotRequest(spotInstanceRequest, testSecurityGroups);
        spotInstanceRequester.waitForActiveInstances(requestIds);

        instanceIds = spotInstanceRequester.getInstanceIds(requestIds);
        for (String instanceId : instanceIds) {
            System.out.println("instanceId: " + instanceId);
        }

        Assertions.assertThat(instanceIds).hasSize(instanceCount);
    }

}