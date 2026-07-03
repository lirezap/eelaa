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
                config.getShutdownQuietPeriodSeconds(), config.getShutdownWaitTimeSeconds(), SECONDS);
    }
}
