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
package software.openx.eelaa.memory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Utility methods useful for working with string memory.
 *
 * @author Alireza Pourtaghi
 */
public final class StringUtil {

    public static int requiredUTF8BytesLength(final String string) {
        return ByteBufUtil.utf8Bytes(string);
    }

    public static int requiredNullTerminatedUTF8BytesLength(final String string) {
        return Math.addExact(requiredUTF8BytesLength(string), 1);
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
