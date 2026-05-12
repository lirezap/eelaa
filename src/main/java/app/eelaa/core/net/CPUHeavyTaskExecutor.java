package app.eelaa.core.net;

import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.Future;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * CPU heavy task executor.
 *
 * @author Alireza Pourtaghi
 */
public final class CPUHeavyTaskExecutor extends DefaultEventExecutorGroup {
    private final CPUHeavyTaskExecutorConfig config;

    public CPUHeavyTaskExecutor(final CPUHeavyTaskExecutorConfig config) {
        super(config.getNThreads());
        this.config = config;
    }

    @Override
    public Future<?> shutdownGracefully() {
        return super.shutdownGracefully(
                config.getShutdownQuitePeriodSeconds(), config.getShutdownWaitTimeSeconds(), SECONDS);
    }
}
