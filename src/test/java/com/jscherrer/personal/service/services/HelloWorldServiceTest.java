package com.jscherrer.personal.service.services;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import javax.ws.rs.core.Response;

public class HelloWorldServiceTest {

    private HelloWorldService helloWorldService = new HelloWorldService();

    @Test
    public void greetName() {
        String inputName = "Amely";
        String expectedResult = "Greetings Amely";

        Response response = helloWorldService.greetName(inputName);
        String responseAsString = (String) response.getEntity();

        Assertions.assertThat(responseAsString).isEqualTo(expectedResult);
    }
}