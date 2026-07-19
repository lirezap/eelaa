/*
 * Copyright 2026 Alireza Pourtaghi <lirezap@protonmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package software.openx.eelaa;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

/**
 * Configuration loading component.
 *
 * @author Alireza Pourtaghi
 */
final class Configuration {
    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

    private final Config config;

    public Configuration() {
        // Loads the following (first-listed are higher priority)
        // system properties
        // application.conf (all resources on classpath with this name)
        // application.json (all resources on classpath with this name)
        // application.properties (all resources on classpath with this name)
        // reference.conf (all resources on classpath with this name)
        this.config = ConfigFactory.load();
    }

    public boolean loadBoolean(final String key) {
        final var value = config.getBoolean(key);
        logger.trace("{}: {}", key, value);

        return value;
    }

    public int loadInt(final String key) {
        final var value = config.getInt(key);
        logger.trace("{}: {}", key, value);

        return value;
    }

    public long loadLong(final String key) {
        final var value = config.getLong(key);
        logger.trace("{}: {}", key, value);

        return value;
    }

    public double loadDouble(final String key) {
        final var value = config.getDouble(key);
        logger.trace("{}: {}", key, value);

        return value;
    }

    public String loadString(final String key) {
        final var value = config.getString(key);
        logger.trace("{}: {}", key, value);

        return value;
    }

    public List<String> loadStringList(final String key) {
        final var value = config.getStringList(key);
        logger.trace("{}: {}", key, value);

        return value;
    }

    public Duration loadDuration(final String key) {
        final var value = config.getDuration(key);
        logger.trace("{}: {}", key, value);

        return value;
    }

    public long loadMemoryBytes(final String key) {
        final var value = config.getMemorySize(key).toBytes();
        logger.trace("{}: {}", key, value);

        return value;
    }

    public Path loadPath(final String key) {
        final var value = Path.of(config.getString(key));
        logger.trace("{}: {}", key, value);

        return value;
    }

    public ConfigObject loadObject(final String key) {
        return config.getObject(key);
    }

    public Config loadConfig(final String key) {
        return config.getConfig(key);
    }
}
