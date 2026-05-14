package app.eelaa.core.net;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * Future listener that releases provided byte buf.
 *
 * @author Alireza Pourtaghi
 */
final class ReleaseByteBufFutureListener implements GenericFutureListener<Future<? super Object>> {
    private final ByteBuf buf;

    public ReleaseByteBufFutureListener(final ByteBuf buf) {
        this.buf = buf;
    }

    @Override
    public void operationComplete(final Future<? super Object> future) throws Exception {
        ReferenceCountUtil.release(buf);
    }
}
