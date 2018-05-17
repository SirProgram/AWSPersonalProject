package com.jscherrer.personal.deployment.iam;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.identitymanagement.model.AttachedPolicy;
import com.amazonaws.services.identitymanagement.model.InstanceProfile;
import com.amazonaws.services.identitymanagement.model.Role;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class IAMRoleManagementTest {

    private final IAMTestHelper IAMTestHelper = new IAMTestHelper(AmazonIdentityManagementClientBuilder.defaultClient());

    private IAMRoleManagement iamRoleManagement;
    private String instanceProfileName;
    private String roleName;

    @Before
    public void setUp() {
        iamRoleManagement = new IAMRoleManagement();
    }

    @After
    public void tearDown() {
        if (instanceProfileName != null) {
            IAMTestHelper.cleanUpInstanceProfile(instanceProfileName);
        }
        if (roleName != null) {
            IAMTestHelper.cleanUpRole(roleName);
        }
    }

    @Test
    public void createRole() throws IOException {
        roleName = UUID.randomUUID().toString();
        String rolePolicyAsString = IAMTestHelper.getTrustPolicyAsString();
        iamRoleManagement.createRole(roleName, rolePolicyAsString);

        Role role = IAMTestHelper.getRole(roleName);
        Assertions.assertThat(role).isNotNull();
        Assertions.assertThat(role.getRoleName()).isEqualTo(roleName);
        String decodedRolePolicy = URLDecoder.decode(role.getAssumeRolePolicyDocument(), String.valueOf(StandardCharsets.UTF_8));
        Assertions.assertThat(decodedRolePolicy).isEqualToIgnoringWhitespace(rolePolicyAsString);
    }

    @Test
    public void createInstanceProfile() {
        instanceProfileName = UUID.randomUUID().toString();
        iamRoleManagement.createInstanceProfile(instanceProfileName);

        InstanceProfile instanceProfileResult = IAMTestHelper.getInstanceProfile(instanceProfileName);
        Assertions.assertThat(instanceProfileResult).isNotNull();
        Assertions.assertThat(instanceProfileResult.getInstanceProfileName()).isEqualTo(instanceProfileName);
    }

    @Test
    public void addRoleToInstanceProfile() throws IOException {
        roleName = givenRoleName();
        instanceProfileName = UUID.randomUUID().toString();
        iamRoleManagement.createInstanceProfile(instanceProfileName);

        iamRoleManagement.addRoleToInstanceProfile(roleName, instanceProfileName);

        InstanceProfile instanceProfileResult = IAMTestHelper.getInstanceProfile(instanceProfileName);
        Assertions.assertThat(instanceProfileResult).isNotNull();
        Assertions.assertThat(instanceProfileResult.getInstanceProfileName()).isEqualTo(instanceProfileName);
        Assertions.assertThat(instanceProfileResult.getRoles()).hasSize(1);
        Assertions.assertThat(instanceProfileResult.getRoles().get(0).getRoleName()).isEqualTo(roleName);
    }

    @Test
    public void attachPolicyToRole() throws IOException {
        roleName = givenRoleName();
        String policyArn = "arn:aws:iam::aws:policy/AmazonS3ReadOnlyAccess";

        iamRoleManagement.attachPolicyToRole(policyArn, roleName);

        List<AttachedPolicy> attachedPolicies = IAMTestHelper.getAttachedRolePolicies(roleName);

        Assertions.assertThat(attachedPolicies).hasSize(1);
        Assertions.assertThat(attachedPolicies.get(0).getPolicyArn()).isEqualTo(policyArn);
    }

    private String givenRoleName() throws IOException {
        roleName = UUID.randomUUID().toString();
        String rolePolicyAsString = IAMTestHelper.getTrustPolicyAsString();
        iamRoleManagement.createRole(roleName, rolePolicyAsString);
        return roleName;
    }
}