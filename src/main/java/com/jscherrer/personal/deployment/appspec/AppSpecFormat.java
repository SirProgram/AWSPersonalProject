package com.jscherrer.personal.deployment.appspec;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class AppSpecFormat {

    public double version = 0.0;
    public String os = "linux";
    public List<LinkedHashMap<String, String>> files = new ArrayList<>();

    public AppSpecFormat withFile(String source, String destination) {
        LinkedHashMap<String, String> appSpecFile = new LinkedHashMap<>();
        appSpecFile.put("source", source);
        appSpecFile.put("destination", destination);
        files.add(appSpecFile);
        return this;
    }
}
