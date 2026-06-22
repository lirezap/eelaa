package software.openx.eelaa.handlers;

import io.netty.buffer.ByteBufAllocator;
import io.netty.util.ReferenceCountUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Alireza Pourtaghi
 */
public class EncodingTest {

    @Test
    public void testFailedTransaction1() {
        var failedTransaction = new FailedTransaction(String.format("%s:%s", System.currentTimeMillis(), 1), "failed");
        var encoded = failedTransaction.encodeV1(ByteBufAllocator.DEFAULT);
        assertEquals(0b00000001, encoded.readByte());
        assertEquals(0b00000000, encoded.readByte());
        assertEquals(failedTransaction.frameBinarySize() - 6, encoded.readInt());

        var decoded = FailedTransaction.decode(encoded);
        assertEquals(failedTransaction.getId(), decoded.getId());
        assertEquals(failedTransaction.getReason(), decoded.getReason());
        ReferenceCountUtil.release(encoded);
    }

    @Test
    public void testFailedTransaction2() {
        var failedTransaction = new FailedTransaction(String.format("%s:%s", System.currentTimeMillis(), 1), null);
        var encoded = failedTransaction.encodeV1(ByteBufAllocator.DEFAULT);
        assertEquals(0b00000001, encoded.readByte());
        assertEquals(0b00000000, encoded.readByte());
        assertEquals(failedTransaction.frameBinarySize() - 6, encoded.readInt());

        var decoded = FailedTransaction.decode(encoded);
        assertEquals(failedTransaction.getId(), decoded.getId());
        assertEquals("", decoded.getReason());
        ReferenceCountUtil.release(encoded);
    }
}
