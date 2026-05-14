package app.eelaa.core.net;

import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.*;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSessionContext;
import java.util.List;

/**
 * TLS context based on {@link SslContext}.
 *
 * @author Alireza Pourtaghi
 */
final class TLSContext extends SslContext {
    private final SslContext context;

    public TLSContext(final TLSContextConfig config) throws Exception {
        this.context = SslContextBuilder
                .forServer(config.getServerCertPath().toFile(), config.getServerKeyPath().toFile())
                .protocols("TLSv1.3")
                .sslProvider(SslProvider.OPENSSL)
                .enableOcsp(config.isUseOcsp())
                .clientAuth(ClientAuth.NONE)
                .sessionCacheSize(config.getSessionCacheSize())
                .sessionTimeout(config.getSessionTimeoutSeconds())
                .build();
    }

    @Override
    public boolean isClient() {
        return context.isClient();
    }

    @Override
    public List<String> cipherSuites() {
        return context.cipherSuites();
    }

    @Override
    public ApplicationProtocolNegotiator applicationProtocolNegotiator() {
        return context.applicationProtocolNegotiator();
    }

    @Override
    public SSLEngine newEngine(final ByteBufAllocator alloc) {
        return context.newEngine(alloc);
    }

    @Override
    public SSLEngine newEngine(final ByteBufAllocator alloc, final String peerHost, final int peerPort) {
        return context.newEngine(alloc, peerHost, peerPort);
    }

    @Override
    public SSLSessionContext sessionContext() {
        return context.sessionContext();
    }
}
