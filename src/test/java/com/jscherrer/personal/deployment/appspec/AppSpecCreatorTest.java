package com.jscherrer.personal.deployment.appspec;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class AppSpecCreatorTest {

    private static final String validAppSpecYamlFilePath = "src/test/resources/validAppSpec.yaml";
    AppSpecCreator creator = new AppSpecCreator();

    @Test
    public void canCreateAppSpecYamlWithCorrectFormat() throws IOException {

        String expectedYamlContents = new String(Files.readAllBytes(Paths.get(validAppSpecYamlFilePath)));

        AppSpecFormat formatToCreate = new AppSpecFormat()
                .withFile("Config/config.txt", "/webapps/Config")
                .withFile("source", "/webapps/myApp");

        String createdAppSpecYaml = creator.createAppSpecYaml(formatToCreate);

        Assertions.assertThat(createdAppSpecYaml).isEqualTo(expectedYamlContents);
    }

    @Test
    public void canWriteYamlToFile() throws IOException {
        String yamlContents = new String(Files.readAllBytes(Paths.get(validAppSpecYamlFilePath)));

        File tmpFileToWriteTo = File.createTempFile("yamlTmpTestFile", ".tmp");
        tmpFileToWriteTo.deleteOnExit();

        creator.writeYamlToFile(yamlContents, tmpFileToWriteTo.getAbsolutePath());

        Assertions.assertThat(tmpFileToWriteTo).exists();

        String createdYamlContents = new String(Files.readAllBytes(Paths.get(tmpFileToWriteTo.getAbsolutePath())));
        Assertions.assertThat(createdYamlContents).isEqualTo(yamlContents);
    }
}