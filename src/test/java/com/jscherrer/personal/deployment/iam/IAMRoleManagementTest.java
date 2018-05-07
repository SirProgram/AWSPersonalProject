package com.jscherrer.personal.deployment.iam;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.identitymanagement.model.*;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

public class IAMRoleManagementTest {

    private static final AmazonIdentityManagement IAM = AmazonIdentityManagementClientBuilder.defaultClient();
    private static final String rolePolicyFilePath = "src/test/resources/TrustRolePolicy.json";

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
            cleanUpInstanceProfile(instanceProfileName);
        }
        if (roleName != null) {
            cleanUpRole(roleName);
        }
    }

    @Test
    public void createRole() throws IOException {
        roleName = UUID.randomUUID().toString();
        String rolePolicyAsString = getRolePolicyAsString();
        iamRoleManagement.createRole(roleName, rolePolicyAsString);

        Role role = getRole(roleName);
        Assertions.assertThat(role).isNotNull();
        Assertions.assertThat(role.getRoleName()).isEqualTo(roleName);
        String decodedRolePolicy = URLDecoder.decode(role.getAssumeRolePolicyDocument(), String.valueOf(StandardCharsets.UTF_8));
        Assertions.assertThat(decodedRolePolicy).isEqualToIgnoringWhitespace(rolePolicyAsString);
    }

    @Test
    public void createInstanceProfile() {
        instanceProfileName = UUID.randomUUID().toString();
        iamRoleManagement.createInstanceProfile(instanceProfileName);

        InstanceProfile instanceProfileResult = getInstanceProfile(instanceProfileName);
        Assertions.assertThat(instanceProfileResult).isNotNull();
        Assertions.assertThat(instanceProfileResult.getInstanceProfileName()).isEqualTo(instanceProfileName);
    }

    @Test
    public void addRoleToInstanceProfile() throws IOException {
        roleName = givenRoleName();
        instanceProfileName = UUID.randomUUID().toString();
        iamRoleManagement.createInstanceProfile(instanceProfileName);

        iamRoleManagement.addRoleToInstanceProfile(roleName, instanceProfileName);

        InstanceProfile instanceProfileResult = getInstanceProfile(instanceProfileName);
        Assertions.assertThat(instanceProfileResult).isNotNull();
        Assertions.assertThat(instanceProfileResult.getInstanceProfileName()).isEqualTo(instanceProfileName);
        Assertions.assertThat(instanceProfileResult.getRoles()).hasSize(1);
        Assertions.assertThat(instanceProfileResult.getRoles().get(0).getRoleName()).isEqualTo(roleName);
    }

    private String givenRoleName() throws IOException {
        roleName = UUID.randomUUID().toString();
        String rolePolicyAsString = getRolePolicyAsString();
        iamRoleManagement.createRole(roleName, rolePolicyAsString);
        return roleName;
    }

    private Role getRole(String roleName) {
        GetRoleRequest getRoleRequest = new GetRoleRequest();
        getRoleRequest.setRoleName(roleName);

        return IAM.getRole(getRoleRequest).getRole();
    }

    private String getRolePolicyAsString() throws IOException {
        return new String(Files.readAllBytes(Paths.get(rolePolicyFilePath)));
    }

    private InstanceProfile getInstanceProfile(String instanceProfileName) {
        GetInstanceProfileRequest instanceProfileRequest = new GetInstanceProfileRequest();
        instanceProfileRequest.setInstanceProfileName(instanceProfileName);

        return IAM.getInstanceProfile(instanceProfileRequest).getInstanceProfile();
    }

    private void cleanUpInstanceProfile(String instanceProfileName) {
        InstanceProfile instanceProfile = getInstanceProfile(instanceProfileName);
        for (Role role : instanceProfile.getRoles()) {
            RemoveRoleFromInstanceProfileRequest removeRoleRequest = new RemoveRoleFromInstanceProfileRequest();
            removeRoleRequest.setRoleName(role.getRoleName());
            removeRoleRequest.setInstanceProfileName(instanceProfileName);
            IAM.removeRoleFromInstanceProfile(removeRoleRequest);
        }

        DeleteInstanceProfileRequest deleteInstanceProfileRequest = new DeleteInstanceProfileRequest();
        deleteInstanceProfileRequest.setInstanceProfileName(instanceProfileName);

        IAM.deleteInstanceProfile(deleteInstanceProfileRequest);
    }

    private void cleanUpRole(String roleName) {
        DeleteRoleRequest deleteRoleRequest = new DeleteRoleRequest();
        deleteRoleRequest.setRoleName(roleName);

        IAM.deleteRole(deleteRoleRequest);
    }
}