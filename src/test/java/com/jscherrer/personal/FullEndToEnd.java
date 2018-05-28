package com.jscherrer.personal;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.IamInstanceProfileSpecification;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.LaunchSpecification;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.identitymanagement.model.InstanceProfile;
import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import com.jscherrer.personal.deployment.AWSConstants;
import com.jscherrer.personal.deployment.DefaultLaunchConfiguration;
import com.jscherrer.personal.deployment.StartUpScript;
import com.jscherrer.personal.deployment.iam.IAMRoleManagement;
import com.jscherrer.personal.deployment.iam.IAMTestHelper;
import com.jscherrer.personal.testhelpers.BaseAwsTester;
import org.apache.http.HttpStatus;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.util.List;
import java.util.UUID;

public class FullEndToEnd extends BaseAwsTester {

    private static final Logger LOG = LoggerFactory.getLogger(FullEndToEnd.class);

    private static final AmazonEC2 EC2 = AmazonEC2ClientBuilder.defaultClient();
    private static final AmazonIdentityManagement IAM = AmazonIdentityManagementClientBuilder.defaultClient();

    private String roleName = UUID.randomUUID().toString();
    private String instanceProfile = UUID.randomUUID().toString();
    private String warAppName = "personal-webapp";

    @Before
    public void setUp() {
        RestAssured.port = AWSConstants.IPV4_PORT;
    }

    @Test
    public void deployAndHitEndpoint() throws IOException {
        setupSecurityGroup();

        LaunchSpecification launchSpec = createLaunchSpecification();

        //Needed for Instance Profile to become ready
        Awaitility.await().atMost(Duration.ONE_MINUTE).pollInterval(Duration.TEN_SECONDS).until(() -> {
            startUpSpotInstance(launchSpec);
        });

        Awaitility.await().atMost(Duration.TWO_MINUTES).pollInterval(Duration.TEN_SECONDS)
                .ignoreException(ConnectException.class).until(() -> {
            Response response = RestAssured.given().get(helloWorldUrlForInstance() + "Amely");

            Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_OK);
            Assertions.assertThat(response.getBody().prettyPrint()).isEqualTo("Greetings Amely");
        });
    }

    private LaunchSpecification createLaunchSpecification() throws IOException {
        LaunchSpecification launchSpec = DefaultLaunchConfiguration.getDefaultLaunchSpecification(EC2);

        IamInstanceProfileSpecification instanceProfileSpec = setUpRoleAndIAM();
        launchSpec.setIamInstanceProfile(instanceProfileSpec);

        String deploymentUserData = StartUpScript.getDefaultStartUpScriptForS3File("s3deploybucket", warAppName + ".war");
        launchSpec.setUserData(deploymentUserData);
        return launchSpec;
    }

    private IamInstanceProfileSpecification setUpRoleAndIAM() throws IOException {
        IAMTestHelper iamTestHelper = new IAMTestHelper(IAM);
        String trustPolicyAsString = iamTestHelper.getTrustPolicyAsString();

        IAMRoleManagement roleManagement = new IAMRoleManagement();
        roleManagement.createRole(roleName, trustPolicyAsString);
        String s3FullAccessPolicy = "arn:aws:iam::aws:policy/AmazonS3FullAccess";

        roleManagement.attachPolicyToRole(s3FullAccessPolicy, roleName);
        roleManagement.createInstanceProfile(instanceProfile);
        roleManagement.addRoleToInstanceProfile(roleName, instanceProfile);

        InstanceProfile instanceProfileResult = iamTestHelper.getInstanceProfile(instanceProfile);
        IamInstanceProfileSpecification instanceProfileSpec = new IamInstanceProfileSpecification();
        instanceProfileSpec.setArn(instanceProfileResult.getArn());

        return instanceProfileSpec;

    }

    private String helloWorldUrlForInstance() {
        List<Instance> instances = spotInstanceRequester.describeInstances(instanceIds);
        String publicIpAddress = instances.get(0).getPublicIpAddress();

        String urlToHit = "http://" + publicIpAddress + ":" + AWSConstants.IPV4_PORT + "/" + warAppName + "/" + "helloworld/";
        LOG.info("helloworld url: " + urlToHit);
        return urlToHit;
    }
}