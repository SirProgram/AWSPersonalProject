package com.jscherrer.personal.deployment.iam;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.identitymanagement.model.AddRoleToInstanceProfileRequest;
import com.amazonaws.services.identitymanagement.model.CreateInstanceProfileRequest;
import com.amazonaws.services.identitymanagement.model.CreateRoleRequest;

public class IAMRoleManagement {

    private static final AmazonIdentityManagement AIM = AmazonIdentityManagementClientBuilder.defaultClient();

    public void createRole(String roleName, String rolePolicy) {
        CreateRoleRequest createRoleRequest = new CreateRoleRequest();
        createRoleRequest.setRoleName(roleName);
        createRoleRequest.setAssumeRolePolicyDocument(rolePolicy);
        AIM.createRole(createRoleRequest);
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
