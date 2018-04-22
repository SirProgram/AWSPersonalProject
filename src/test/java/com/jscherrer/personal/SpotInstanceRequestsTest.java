package com.jscherrer.personal;

import com.amazonaws.services.ec2.model.RequestSpotInstancesRequest;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class SpotInstanceRequestsTest {

    private SpotInstanceRequests spotInstanceRequests = new SpotInstanceRequests();
    private ArrayList<String> requestIds;
    private ArrayList<String> instanceIds;

    @Before
    public void setUp() {
        requestIds = new ArrayList<>();
        instanceIds = new ArrayList<>();
    }

    @After
    public void tearDown() {
        spotInstanceRequests.stopSpotRequest(requestIds);
        spotInstanceRequests.stopInstances(instanceIds);
    }

    @Test
    public void instanceRequestCreatesTwoInstances() {
        RequestSpotInstancesRequest spotInstanceRequest = new RequestSpotInstancesRequest();
        spotInstanceRequest.setSpotPrice("0.03");
        int instanceCount = 2;
        spotInstanceRequest.setInstanceCount(instanceCount);

        requestIds = spotInstanceRequests.makeSpotRequest(spotInstanceRequest);
        spotInstanceRequests.waitForActiveInstances(requestIds);

        instanceIds = spotInstanceRequests.getInstanceIds(requestIds);
        for (String instanceId : instanceIds) {
            System.out.println("instanceId: " + instanceId);
        }

        Assertions.assertThat(instanceIds).hasSize(instanceCount);
    }

}