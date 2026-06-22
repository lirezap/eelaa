package software.openx.eelaa.binary;

import io.netty.buffer.ByteBufUtil;

/**
 * @author Alireza Pourtaghi
 */
public final class StringUtil {

    public static int requiredUTF8BytesLength(final String string) {
        return ByteBufUtil.utf8Bytes(string);
    }

    public static int requiredNullTerminatedUTF8BytesLength(final String string) {
        return Math.addExact(ByteBufUtil.utf8Bytes(string), 1);
    }
}
