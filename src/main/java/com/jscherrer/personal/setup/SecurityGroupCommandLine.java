package com.jscherrer.personal.setup;

import com.jscherrer.personal.AWSConstants;

public class SecurityGroupCommandLine {

    private static final SecurityGroupCreator createSecurityGroup = new SecurityGroupCreator();

    public static void main(String[] args) {
        String securityGroupName = AWSConstants.SECURITY_GROUP_NAME;
        String securityGroupDescription = "Security Group created by AWSPersonalProject";

        if (args.length == 1) {
            securityGroupName = args[0];
        }
        if (args.length == 2) {
            securityGroupName = args[0];
            securityGroupDescription = args[1];
        }

        System.out.println("Creating security group with name " + securityGroupName);
        System.out.println("Description " + securityGroupDescription);

        createSecurityGroup.createSecurityGroup(securityGroupName, securityGroupDescription);
    }
}
