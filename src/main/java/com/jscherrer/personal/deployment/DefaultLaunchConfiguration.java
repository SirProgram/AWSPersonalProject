package com.jscherrer.personal.deployment;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeKeyPairsRequest;
import com.amazonaws.services.ec2.model.DescribeKeyPairsResult;
import com.amazonaws.services.ec2.model.LaunchSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class DefaultLaunchConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultLaunchConfiguration.class);

    public static LaunchSpecification getDefaultLaunchSpecification(AmazonEC2 EC2) {
        LaunchSpecification launchSpecification = new LaunchSpecification();
        launchSpecification.setImageId(AWSConstants.DEFAULT_AMI);
        launchSpecification.setInstanceType(AWSConstants.DEFAULT_INSTANCE_TYPE);

        ArrayList<String> securityGroups = new ArrayList<>();
        securityGroups.add(AWSConstants.SECURITY_GROUP_NAME);
        launchSpecification.setSecurityGroups(securityGroups);
        launchSpecification.setKeyName(getKeyPairName(EC2));
        return launchSpecification;
    }

    private static String getKeyPairName(AmazonEC2 EC2) {
        DescribeKeyPairsRequest keyPairsRequest = new DescribeKeyPairsRequest();
        DescribeKeyPairsResult describeKeyPairsResult = EC2.describeKeyPairs(keyPairsRequest);

        describeKeyPairsResult.getKeyPairs();
        if (describeKeyPairsResult.getKeyPairs().size() == 1) {
            return describeKeyPairsResult.getKeyPairs().get(0).getKeyName();
        } else {
            LOG.warn("Multiple KeyPairNames exist, not authing any");
            return null;
        }
    }
}
