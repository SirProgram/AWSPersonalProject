#!/bin/bash
yum -y install java-1.8.0
yum -y remove java-1.7.0-openjdk
yum -y install tomcat7
aws s3 cp s3://${s3Path}/${warName} /var/lib/tomcat7/webapps/${warName}
service tomcat7 start
