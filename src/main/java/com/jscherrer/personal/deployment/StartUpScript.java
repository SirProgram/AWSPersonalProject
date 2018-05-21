package com.jscherrer.personal.deployment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class StartUpScript {

    private static final String startUpScriptPath = "src/main/resources/copyWarFromS3.sh";

    public static String getDefaultStartUpScriptForS3File(String s3Path, String warName) throws IOException {
        String startUpScript = new String(Files.readAllBytes(Paths.get(startUpScriptPath)));
        startUpScript = replacePropertyInScript(startUpScript, "s3Path", s3Path);
        startUpScript = replacePropertyInScript(startUpScript, "warName", warName);

        byte[] encodedScript = Base64.getEncoder().encode(startUpScript.getBytes());

        return new String(encodedScript);
    }

    public static String replacePropertyInScript(String scriptString, String propertyName, String value) {
        return scriptString.replaceAll("\\$\\{" + propertyName + "\\}", value);
    }



}
