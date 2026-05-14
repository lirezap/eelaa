package app.eelaa.core.net;

import io.netty.bootstrap.ServerBootstrap;

/**
 * Native configured server bootstrap.
 *
 * @author Alireza Pourtaghi
 */
final class NativeServerBootstrap extends ServerBootstrap {
    private NativeServerBootstrap() {
    }

    public static NativeServerBootstrap newInstance() {
        return new NativeServerBootstrap();
    }
}
