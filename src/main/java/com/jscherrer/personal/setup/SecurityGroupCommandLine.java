package com.jscherrer.personal.setup;

import com.jscherrer.personal.aws.AWSConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;

public class SecurityGroupCommandLine {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityGroupCommandLine.class);
    private static final SecurityGroupCreator createSecurityGroup = new SecurityGroupCreator();

    public static void main(String[] args) throws UnknownHostException {
        String securityGroupName = AWSConstants.SECURITY_GROUP_NAME;
        String securityGroupDescription = "Security Group created by AWSPersonalProject";

        if (args.length == 1) {
            securityGroupName = args[0];
        }
        if (args.length == 2) {
            securityGroupName = args[0];
            securityGroupDescription = args[1];
        }

        LOG.info("Creating security group with name " + securityGroupName);
        LOG.info("Description " + securityGroupDescription);

        createSecurityGroup.createSecurityGroup(securityGroupName, securityGroupDescription);
    }
}
