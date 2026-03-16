package com.swifttrack.AdminService.conf;

import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Custom PropertySourceFactory for loading .env files.
 * Supports KEY=value format, comments, and quoted values.
 */
public class EnvPropertySourceFactory implements PropertySourceFactory {

    private static final Pattern ENV_VAR_PATTERN = Pattern.compile("^([^=]+)=(.*)$");

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        Properties props = new Properties();

        if (resource.getResource().exists()) {
            String content = new String(Files.readAllBytes(Paths.get(resource.getResource().getFile().toURI())));
            parseEnvContent(content, props);
        }

        return new PropertiesPropertySource(
                name != null ? name : resource.getResource().getFilename(),
                props);
    }

    private void parseEnvContent(String content, Properties props) {
        String[] lines = content.split("\\n");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
                continue;
            }

            Matcher matcher = ENV_VAR_PATTERN.matcher(line);
            if (matcher.find()) {
                String key = matcher.group(1).trim();
                String value = matcher.group(2).trim();

                if ((value.startsWith("\"") && value.endsWith("\"")) ||
                        (value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                }

                props.setProperty(key, value);
            }
        }
    }
}
