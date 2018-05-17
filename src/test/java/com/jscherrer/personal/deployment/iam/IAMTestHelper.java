package com.jscherrer.personal.deployment.iam;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class IAMTestHelper {

    private static final String rolePolicyFilePath = "src/test/resources/TrustRolePolicy.json";
    private AmazonIdentityManagement IAM;

    public IAMTestHelper(AmazonIdentityManagement IAM) {
        this.IAM = IAM;
    }

    public Role getRole(String roleName) {
        GetRoleRequest getRoleRequest = new GetRoleRequest();
        getRoleRequest.setRoleName(roleName);

        return IAM.getRole(getRoleRequest).getRole();
    }

    public String getTrustPolicyAsString() throws IOException {
        return new String(Files.readAllBytes(Paths.get(rolePolicyFilePath)));
    }

    public InstanceProfile getInstanceProfile(String instanceProfileName) {
        GetInstanceProfileRequest instanceProfileRequest = new GetInstanceProfileRequest();
        instanceProfileRequest.setInstanceProfileName(instanceProfileName);

        return IAM.getInstanceProfile(instanceProfileRequest).getInstanceProfile();
    }

    public List<AttachedPolicy> getAttachedRolePolicies(String roleName) {
        ListAttachedRolePoliciesRequest attachedRolePoliciesRequest = new ListAttachedRolePoliciesRequest();
        attachedRolePoliciesRequest.setRoleName(roleName);

        return IAM.listAttachedRolePolicies(attachedRolePoliciesRequest).getAttachedPolicies();
    }

    public void cleanUpAttachedRolePolicies(String roleName) {
        List<AttachedPolicy> attachedRolePolicies = getAttachedRolePolicies(roleName);
        for (AttachedPolicy attachedRolePolicy : attachedRolePolicies) {
            DetachRolePolicyRequest detachRequest = new DetachRolePolicyRequest();
            detachRequest.setRoleName(roleName);
            detachRequest.setPolicyArn(attachedRolePolicy.getPolicyArn());

            IAM.detachRolePolicy(detachRequest);
        }
    }

    public void cleanUpInstanceProfile(String instanceProfileName) {
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

    public void cleanUpRole(String roleName) {
        cleanUpAttachedRolePolicies(roleName);

        DeleteRoleRequest deleteRoleRequest = new DeleteRoleRequest();
        deleteRoleRequest.setRoleName(roleName);

        IAM.deleteRole(deleteRoleRequest);
    }
}