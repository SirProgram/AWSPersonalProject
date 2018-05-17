package com.jscherrer.personal.deployment.iam;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.identitymanagement.model.*;

public class IAMRoleManagement {

    private static final AmazonIdentityManagement AIM = AmazonIdentityManagementClientBuilder.defaultClient();

    public void createRole(String roleName, String trustPolicy) {
        CreateRoleRequest createRoleRequest = new CreateRoleRequest();
        createRoleRequest.setRoleName(roleName);
        createRoleRequest.setAssumeRolePolicyDocument(trustPolicy);
        AIM.createRole(createRoleRequest);
    }

    public void attachPolicyToRole(String policyArn, String roleName) {
        AttachRolePolicyRequest attachRolePolicyRequest = new AttachRolePolicyRequest();
        attachRolePolicyRequest.setPolicyArn(policyArn);
        attachRolePolicyRequest.setRoleName(roleName);
        AIM.attachRolePolicy(attachRolePolicyRequest);
    }

    public InstanceProfile getInstanceProfile(String name) {
        GetInstanceProfileRequest getInstanceRequest = new GetInstanceProfileRequest();
        getInstanceRequest.setInstanceProfileName(name);
        return AIM.getInstanceProfile(getInstanceRequest).getInstanceProfile();
    }

    public void createInstanceProfile(String name) {
        CreateInstanceProfileRequest createInstanceProfileRequest = new CreateInstanceProfileRequest();
        createInstanceProfileRequest.setInstanceProfileName(name);
        AIM.createInstanceProfile(createInstanceProfileRequest);
    }

    public void addRoleToInstanceProfile(String roleName, String instanceProfileName) {
        AddRoleToInstanceProfileRequest addRoleRequest = new AddRoleToInstanceProfileRequest();
        addRoleRequest.setRoleName(roleName);
        addRoleRequest.setInstanceProfileName(instanceProfileName);
        AIM.addRoleToInstanceProfile(addRoleRequest);
    }

}
