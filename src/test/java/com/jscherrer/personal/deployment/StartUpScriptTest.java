package com.jscherrer.personal.deployment;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.io.IOException;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class StartUpScriptTest {

    @Test
    public void defaultScriptHasAllPropertiesReplaced() throws IOException {
        String defaultScript = StartUpScript.getDefaultStartUpScriptForS3File("S3Path", "AppName");
        Pattern dollarBraceWrappedCharacters = Pattern.compile("\\$\\{.+\\}");
        Assertions.assertThat(defaultScript).doesNotContainPattern(dollarBraceWrappedCharacters);
    }

    @Test
    public void replacePropertyInScript() {
        String scriptString = "My replaced variable is now ${replaceMe}";
        String expectedString = "My replaced variable is now here";

        Assertions.assertThat(StartUpScript.replacePropertyInScript(scriptString, "replaceMe", "here"))
                .isEqualTo(expectedString);
    }
}