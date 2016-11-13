package org.bitbucket.pshirshov.izumitk.app;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 *
 */
public final class Version {
    private static final String DEFAULT_VALUE_UNKNOWN = "UNKNOWN";
    private static final Pattern SEMICOLON = Pattern.compile(":");
    private static final Pattern NEWLINE = Pattern.compile("\n");

    private static final Map<String, String> MANIFEST;

    static {
        URL resource = ClassLoader.getSystemClassLoader().getResource("META-INF/MANIFEST.MF");

        Map<String, String> parsed = new HashMap<>();

        if (resource != null) {
            try (InputStream inputStream = resource.openStream()) {
                String manifest = IOUtils.toString(inputStream);

                for (String line : NEWLINE.split(manifest)) {
                    String[] entries = SEMICOLON.split(line);

                    if (entries.length >= 2) {
                        String key = entries[0].trim();
                        String value = StringUtils.join(ArrayUtils.subarray(entries, 1, entries.length), ":");
                        parsed.put(key, value.trim());
                    }
                }
            } catch (IOException ignored) {
            }
        }

        MANIFEST = ImmutableMap.<String, String>builder().putAll(parsed).build();
    }

    private Version() {
    }


    public static String buildInfo() {
        return MessageFormat.format("Build[{0}]",
                                    buildString());
    }


    public static String buildString() {
        return MessageFormat.format("version: {0}, timestamp: {1}, user {2}, revision: {3}",
                                    getVersion(),
                                    getBuildTimestamp(),
                                    getBuildUser(),
                                    getRevision());
    }


    public static String getVersion() {
        Optional<String> manifestVersion = Optional.ofNullable(MANIFEST.get("X-Version"));
        if (manifestVersion.isPresent()) {
            return manifestVersion.get();
        }

        // fallback to using Java API
        Package aPackage = Version.class.getPackage();
        if (aPackage != null) {
            String version = aPackage.getImplementationVersion();
            if (version == null) {
                String specificationVersion = aPackage.getSpecificationVersion();
                if (specificationVersion != null) {
                    return specificationVersion;
                }
            }
        }

        return DEFAULT_VALUE_UNKNOWN;
    }


    public static String getBuildTimestamp() {
        String timestamp = MANIFEST.get("Git-Build-Date");
        if (timestamp != null) {
            return timestamp;
        }

        return DEFAULT_VALUE_UNKNOWN;
    }


    public static String getBuildUser() {
        String user = MANIFEST.get("X-Built-By");
        if (user == null) {
            return DEFAULT_VALUE_UNKNOWN;
        }
        return user;
    }


    public static String getRevision() {
        Optional<String> revision = Optional.ofNullable(MANIFEST.get("Git-Branch"));
        Optional<String> head = Optional.ofNullable(MANIFEST.get("Git-Head-Rev"));
        Optional<String> clean = Optional.ofNullable(MANIFEST.get("Git-Repo-Is-Clean"));

        final String isClean;
        if (Boolean.valueOf(clean.orElse("false"))) {
            isClean = "";
        } else {
            isClean = "*";
        }

        return MessageFormat.format("{0}:{1}{2}",
                                    revision.orElse(DEFAULT_VALUE_UNKNOWN),
                                    head.orElse(DEFAULT_VALUE_UNKNOWN),
                                    isClean
        );
    }
}
