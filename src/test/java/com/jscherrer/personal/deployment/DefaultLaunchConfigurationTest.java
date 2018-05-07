package com.jscherrer.personal.deployment;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeKeyPairsResult;
import com.amazonaws.services.ec2.model.KeyPair;
import com.amazonaws.services.ec2.model.KeyPairInfo;
import com.amazonaws.services.ec2.model.LaunchSpecification;
import jersey.repackaged.com.google.common.collect.Iterables;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import static org.junit.Assert.*;

public class DefaultLaunchConfigurationTest {

    @Test
    public void launchSpecificationWithNoRoles() {
        Collection<KeyPairInfo> keyPairInfos = mockNumberOfKeyPairsInfos(0);
        AmazonEC2 mockEC2 = getMockEC2WithKeyPairs(keyPairInfos);

        LaunchSpecification defaultLaunchSpecification = DefaultLaunchConfiguration.getDefaultLaunchSpecification(mockEC2);

        Assertions.assertThat(defaultLaunchSpecification.getImageId()).isNotEmpty();
        Assertions.assertThat(defaultLaunchSpecification.getInstanceType()).isNotEmpty();
        Assertions.assertThat(defaultLaunchSpecification.getSecurityGroups()).isNotEmpty();

        Assertions.assertThat(defaultLaunchSpecification.getKeyName()).isNull();
    }

    @Test
    public void launchSpecificationWithARoleHasKeyName() {
        Collection<KeyPairInfo> keyPairInfos = mockNumberOfKeyPairsInfos(1);
        KeyPairInfo firstKeyPairInfo = keyPairInfos.iterator().next();
        AmazonEC2 mockEC2 = getMockEC2WithKeyPairs(keyPairInfos);

        LaunchSpecification defaultLaunchSpecification = DefaultLaunchConfiguration.getDefaultLaunchSpecification(mockEC2);

        Assertions.assertThat(defaultLaunchSpecification.getImageId()).isNotEmpty();
        Assertions.assertThat(defaultLaunchSpecification.getInstanceType()).isNotEmpty();
        Assertions.assertThat(defaultLaunchSpecification.getSecurityGroups()).isNotEmpty();

        Assertions.assertThat(defaultLaunchSpecification.getKeyName()).isEqualTo(firstKeyPairInfo.getKeyName());
    }

    @Test
    public void launchSpecificationWithMultipleRolesHasNoKeyName() {
        Collection<KeyPairInfo> keyPairInfos = mockNumberOfKeyPairsInfos(2);
        AmazonEC2 mockEC2 = getMockEC2WithKeyPairs(keyPairInfos);

        LaunchSpecification defaultLaunchSpecification = DefaultLaunchConfiguration.getDefaultLaunchSpecification(mockEC2);

        Assertions.assertThat(defaultLaunchSpecification.getImageId()).isNotEmpty();
        Assertions.assertThat(defaultLaunchSpecification.getInstanceType()).isNotEmpty();
        Assertions.assertThat(defaultLaunchSpecification.getSecurityGroups()).isNotEmpty();

        Assertions.assertThat(defaultLaunchSpecification.getKeyName()).isNull();
    }

    private AmazonEC2 getMockEC2WithKeyPairs(Collection<KeyPairInfo> keyPairInfos) {
        AmazonEC2 mockEC2 = Mockito.mock(AmazonEC2.class);
        DescribeKeyPairsResult mockedResult = new DescribeKeyPairsResult();
        mockedResult.setKeyPairs(keyPairInfos);

        Mockito.when(mockEC2.describeKeyPairs(Mockito.any())).thenReturn(mockedResult);
        return mockEC2;
    }

    private Collection<KeyPairInfo> mockNumberOfKeyPairsInfos(int number) {
        ArrayList<KeyPairInfo> keyPairInfos = new ArrayList<>();

        for(int i = 0; i < number; i++) {
            keyPairInfos.add(randomKeyPairInfo());
        }

        return keyPairInfos;
    }

    private KeyPairInfo randomKeyPairInfo() {
        KeyPairInfo keyPairInfo = new KeyPairInfo();
        keyPairInfo.setKeyName(UUID.randomUUID().toString());
        return keyPairInfo;
    }
}