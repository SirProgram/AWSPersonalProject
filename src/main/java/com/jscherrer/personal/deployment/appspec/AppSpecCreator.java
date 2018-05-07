package com.jscherrer.personal.deployment.appspec;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;

public class AppSpecCreator {

    public String createAppSpecYaml(AppSpecFormat config) {
        DumperOptions options = createYamlOptions();
        Yaml appSpecYaml = new Yaml(options);

        LinkedHashMap<String, Object> appFileTopLevel = new LinkedHashMap<>();
        appFileTopLevel.put("version", config.version);
        appFileTopLevel.put("os", config.os);
        appFileTopLevel.put("files", config.files);

        return appSpecYaml.dump(appFileTopLevel);
    }

    public File writeYamlToFile(String yamlContents, String destination) throws IOException {
        File destinationFile = new File(destination);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(destinationFile))) {
            writer.write(yamlContents);
        }

        return destinationFile;
    }

    private DumperOptions createYamlOptions() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndent(4);
        options.setIndicatorIndent(2);
        options.setLineBreak(DumperOptions.LineBreak.UNIX);
        return options;
    }

}
