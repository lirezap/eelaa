package software.openx.eelaa.net;

import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.Future;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * CPU heavy task executor.
 *
 * @author Alireza Pourtaghi
 */
final class CPUHeavyTaskExecutor extends DefaultEventExecutorGroup {
    private final CPUHeavyTaskExecutorConfig config;

    private CPUHeavyTaskExecutor(final CPUHeavyTaskExecutorConfig config) {
        super(config.getNThreads());
        this.config = config;
    }

    public static CPUHeavyTaskExecutor newInstance(final CPUHeavyTaskExecutorConfig config) {
        return new CPUHeavyTaskExecutor(config);
    }

    @Override
    public Future<?> shutdownGracefully() {
        return super.shutdownGracefully(
                config.getShutdownQuitePeriodSeconds(), config.getShutdownWaitTimeSeconds(), SECONDS);
    }
}
