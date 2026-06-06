package software.openx.eelaa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application class to be executed.
 *
 * @author Alireza Pourtaghi
 */
public final class EelaaApplication {
    private static final Logger logger = LoggerFactory.getLogger(EelaaApplication.class);

    public static void main(final String... args) {
        logger.info("Starting eelaa version: {}", EelaaApplication.class.getPackage().getImplementationVersion());
    }
}
