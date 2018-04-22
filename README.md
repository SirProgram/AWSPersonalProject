# AWSPersonalProject

This is a personal project written in Java aiming to have a couple of instances hosted in AWS,
on demand using spot instances, with the instances able to call and use AWS features.

## Running this project

To build this project you will need to have the following on your machine:
- Java 8 SDK
- AWS Credentials
- Maven

#### Setup AWS Credentials
This project requires you to have an AWS account and set up AWS credentials and config on your machine.
This means you must have a valid `credentials` file in your home directory at: `~/.aws/`

For more information on creating this file see:
https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/signup-create-iam-user.html
https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/setup-credentials.html

#### Building using maven
To build the project locally, run the following maven command:

`maven install`

This will build the classes, run the tests and build a war file under `target/personal-webapp.war`
