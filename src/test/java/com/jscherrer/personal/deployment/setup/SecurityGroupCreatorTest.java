package com.jscherrer.personal.deployment.setup;

import com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import com.jscherrer.personal.testhelpers.BaseAwsTester;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SecurityGroupCreatorTest extends BaseAwsTester {

    private String expectedSecurityGroupName;

    @Before
    public void setUp() {
        expectedSecurityGroupName = UUID.randomUUID().toString();
    }

    @After
    public void tearDown() {
        cleanUpSecurityGroup(expectedSecurityGroupName);
    }

    @Test
    public void canCreateSecurityGroup() throws UnknownHostException {
        securityGroupCreator.createSecurityGroup(expectedSecurityGroupName,
                "Security Group created from SecurityGroupCreatorTest");

        ArrayList<String> securityGroupNames = new ArrayList<>();
        securityGroupNames.add(expectedSecurityGroupName);

        Awaitility.await().atMost(Duration.TEN_SECONDS).until(() ->
                Assertions.assertThat(getSecurityGroups(securityGroupNames))
                        .extracting(SecurityGroup::getGroupName)
                        .containsExactly(expectedSecurityGroupName));
    }

    private List<SecurityGroup> getSecurityGroups(ArrayList<String> securityGroupNames) {
        DescribeSecurityGroupsRequest describeSecurityGroupsRequest = new DescribeSecurityGroupsRequest();
        describeSecurityGroupsRequest.setGroupNames(securityGroupNames);

        DescribeSecurityGroupsResult describeSecurityGroupsResult = EC2.describeSecurityGroups(describeSecurityGroupsRequest);
        return describeSecurityGroupsResult.getSecurityGroups();
    }

    private void cleanUpSecurityGroup(String securityGroupName) {
        DeleteSecurityGroupRequest deleteSecurityGroupRequest = new DeleteSecurityGroupRequest();
        deleteSecurityGroupRequest.setGroupName(securityGroupName);

        EC2.deleteSecurityGroup(deleteSecurityGroupRequest);
    }

}