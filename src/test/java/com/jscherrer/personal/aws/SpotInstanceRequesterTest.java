package com.jscherrer.personal.aws;

import com.amazonaws.services.ec2.model.RequestSpotInstancesRequest;
import com.jscherrer.personal.testhelpers.BaseAwsTester;
import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;

public class SpotInstanceRequesterTest extends BaseAwsTester {

    @BeforeClass
    public static void setUp() {
        setupSecurityGroup();
    }

    @Test
    public void instanceRequestCreatesTwoInstances() {
        RequestSpotInstancesRequest spotInstanceRequest = new RequestSpotInstancesRequest();
        spotInstanceRequest.setSpotPrice("0.03");
        int instanceCount = 2;
        spotInstanceRequest.setInstanceCount(instanceCount);

        requestIds = spotInstanceRequester.makeSpotRequest(spotInstanceRequest);
        spotInstanceRequester.waitForActiveInstances(requestIds);

        instanceIds = spotInstanceRequester.getInstanceIds(requestIds);
        for (String instanceId : instanceIds) {
            System.out.println("instanceId: " + instanceId);
        }

        Assertions.assertThat(instanceIds).hasSize(instanceCount);
    }

}