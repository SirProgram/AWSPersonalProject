package com.jscherrer.personal.setup;

import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.jscherrer.personal.testhelpers.BaseAwsTester;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SecurityGroupCreatorTest extends BaseAwsTester {

    @Test
    public void canCreateSecurityGroup() throws UnknownHostException {
        String expectedSecurityGroupName = UUID.randomUUID().toString();

        securityGroupCreator.createSecurityGroup(expectedSecurityGroupName,
                "Security Group created from SecurityGroupCreatorTest");

        ArrayList<String> securityGroupNames = new ArrayList<>();
        securityGroupNames.add(expectedSecurityGroupName);

        Assertions.assertThat(getSecurityGroups(securityGroupNames))
                .extracting(SecurityGroup::getGroupName)
                .containsExactly(expectedSecurityGroupName);
    }

    private List<SecurityGroup> getSecurityGroups(ArrayList<String> securityGroupNames) {
        DescribeSecurityGroupsRequest describeSecurityGroupsRequest = new DescribeSecurityGroupsRequest();
        describeSecurityGroupsRequest.setGroupNames(securityGroupNames);

        DescribeSecurityGroupsResult describeSecurityGroupsResult = EC2.describeSecurityGroups(describeSecurityGroupsRequest);
        return describeSecurityGroupsResult.getSecurityGroups();
    }

}