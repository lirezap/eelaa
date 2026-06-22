package software.openx.eelaa.binary;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

import static java.nio.charset.StandardCharsets.UTF_8;

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

    public static String readNullTerminatedUTF8String(final ByteBuf buf) {
        final var readerIndex = buf.readerIndex();
        var length = 0;
        while (buf.readByte() != 0x00) {
            length++;
        }

        buf.readerIndex(readerIndex);
        final var string = buf.readString(length, UTF_8);
        buf.readByte(); // Null value itself.

        return string;
    }
}
