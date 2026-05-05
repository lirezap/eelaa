package app.eelaa.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application class to be executed.
 *
 * @author Alireza Pourtaghi
 */
public final class CoreApplication {
    private static final Logger logger = LoggerFactory.getLogger(CoreApplication.class);

    public static void main(String... args) {
        logger.info("Starting core version: {}", CoreApplication.class.getPackage().getImplementationVersion());
    }
}
