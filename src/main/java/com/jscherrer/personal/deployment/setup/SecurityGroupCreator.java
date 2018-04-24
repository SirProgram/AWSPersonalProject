package com.jscherrer.personal.deployment.setup;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.IpRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class SecurityGroupCreator {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityGroupCreator.class);

    public void createSecurityGroup(String securityGroupName, String securityGroupDescription) throws UnknownHostException {
        AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        requestNewSecurityGroup(ec2, securityGroupName, securityGroupDescription);
        String hostIpAddress = getHostIpAddress();
        ArrayList<IpRange> ipRanges = new ArrayList<>();
        ipRanges.add(new IpRange().withCidrIp(hostIpAddress));

        ArrayList<IpPermission> ipPermissions = createIpPermissions(ipRanges);

        authorizeSecurityGroup(ec2, ipPermissions, securityGroupName);
    }

    private boolean authorizeSecurityGroup(AmazonEC2 ec2, ArrayList<IpPermission> ipPermissions, String securityGroupName) {
        try {
            AuthorizeSecurityGroupIngressRequest ingressRequest =
                    new AuthorizeSecurityGroupIngressRequest(securityGroupName, ipPermissions);
            ec2.authorizeSecurityGroupIngress(ingressRequest);
            return true;
        } catch (AmazonServiceException ase) {
            //Already created?
            LOG.warn(ase.getMessage());
        }
        return false;
    }

    private ArrayList<IpPermission> createIpPermissions(ArrayList<IpRange> ipRanges) {
        ArrayList<IpPermission> ipPermissions = new ArrayList<>();
        IpPermission ipPermission = new IpPermission();
        ipPermission.setIpProtocol("tcp");
        ipPermission.setFromPort(22);
        ipPermission.setToPort(22);
        ipPermission.setIpv4Ranges(ipRanges);
        ipPermissions.add(ipPermission);
        return ipPermissions;
    }

    private String getHostIpAddress() throws UnknownHostException {
        InetAddress hostAddress = InetAddress.getLocalHost();
        return hostAddress.getHostAddress() + "/32";
    }

    private boolean requestNewSecurityGroup(AmazonEC2 ec2, String securityGroupName, String securityGroupDescription) {
        try {
            CreateSecurityGroupRequest securityGroupRequest = new CreateSecurityGroupRequest(securityGroupName, securityGroupDescription);
            ec2.createSecurityGroup(securityGroupRequest);
            return true;
        } catch (AmazonServiceException ase) {
            //Already created?
            LOG.warn(ase.getMessage());
        }
        return false;
    }


}
