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
    private final TLSContextConfig config;
    private final SslContext context;

    public TLSContext(final TLSContextConfig config, final SslContext context) {
        this.config = config;
        this.context = context;
    }

    public static TLSContext newInstance(final TLSContextConfig config) throws Exception {
        final var sslContext = SslContextBuilder
                .forServer(config.getServerCertPath().toFile(), config.getServerKeyPath().toFile())
                .sslProvider(SslProvider.OPENSSL_REFCNT)
                .protocols("TLSv1.3")
                .enableOcsp(config.isUseOcsp())
                .clientAuth(ClientAuth.NONE)
                .sessionCacheSize(config.getSessionCacheSize())
                .sessionTimeout(config.getSessionTimeoutSeconds())
                .build();

        return new TLSContext(config, sslContext);
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

    public TLSContextConfig config() {
        return config;
    }
}
