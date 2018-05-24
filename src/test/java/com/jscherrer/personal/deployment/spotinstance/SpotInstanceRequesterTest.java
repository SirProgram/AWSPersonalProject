package com.jscherrer.personal.deployment.spotinstance;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RequestSpotInstancesRequest;
import com.jscherrer.personal.testhelpers.AWSTestConstants;
import com.jscherrer.personal.testhelpers.BaseAwsTester;
import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class SpotInstanceRequesterTest extends BaseAwsTester {

    private static final Logger LOG = LoggerFactory.getLogger(SpotInstanceRequesterTest.class);

    @BeforeClass
    public static void setUp() throws UnknownHostException {
        setupSecurityGroup();
    }

    @Test
    public void instanceRequestCanCreateOneInstance() {
        RequestSpotInstancesRequest spotInstanceRequest = new RequestSpotInstancesRequest();
        spotInstanceRequest.setSpotPrice("0.03");
        int instanceCount = 1;
        spotInstanceRequest.setInstanceCount(instanceCount);

        makeRequestUsingInstanceRequest(spotInstanceRequest);

        Assertions.assertThat(instanceIds).hasSize(instanceCount);
    }

    @Test
    public void instanceRequestCanCreateTwoInstances() {
        RequestSpotInstancesRequest spotInstanceRequest = new RequestSpotInstancesRequest();
        spotInstanceRequest.setSpotPrice("0.03");
        int instanceCount = 2;
        spotInstanceRequest.setInstanceCount(instanceCount);

        makeRequestUsingInstanceRequest(spotInstanceRequest);

        Assertions.assertThat(instanceIds).hasSize(instanceCount);
    }

    @Test
    public void canDescribeOneInstance() {
        RequestSpotInstancesRequest spotInstanceRequest = new RequestSpotInstancesRequest();
        spotInstanceRequest.setSpotPrice("0.03");
        int instanceCount = 1;
        spotInstanceRequest.setInstanceCount(instanceCount);

        makeRequestUsingInstanceRequest(spotInstanceRequest);
        Assertions.assertThat(instanceIds).hasSize(instanceCount);

        List<Instance> instances = spotInstanceRequester.describeInstances(instanceIds);
        Assertions.assertThat(instances).hasSize(instanceCount);
        Assertions.assertThat(instances.get(0).getInstanceId()).isEqualTo(instanceIds.get(0));
    }

    @Test
    public void canDescribeTwoInstances() {
        RequestSpotInstancesRequest spotInstanceRequest = new RequestSpotInstancesRequest();
        spotInstanceRequest.setSpotPrice("0.03");
        int instanceCount = 2;
        spotInstanceRequest.setInstanceCount(instanceCount);

        makeRequestUsingInstanceRequest(spotInstanceRequest);
        Assertions.assertThat(instanceIds).hasSize(instanceCount);

        List<Instance> instances = spotInstanceRequester.describeInstances(instanceIds);
        Assertions.assertThat(instances).hasSize(instanceCount);
        Assertions.assertThat(instances)
                .extracting(Instance::getInstanceId)
                .containsExactlyInAnyOrderElementsOf(instanceIds);
    }

    private void makeRequestUsingInstanceRequest(RequestSpotInstancesRequest spotInstanceRequest) {
        ArrayList<String> testSecurityGroups = new ArrayList<>();
        testSecurityGroups.add(AWSTestConstants.SECURITY_GROUP_NAME);

        requestIds = spotInstanceRequester.makeSpotRequest(spotInstanceRequest, testSecurityGroups);
        spotInstanceRequester.waitForActiveInstances(requestIds);

        instanceIds = spotInstanceRequester.getInstanceIds(requestIds);

        for (String instanceId : instanceIds) {
            LOG.info("instanceId: " + instanceId);
        }
    }

}