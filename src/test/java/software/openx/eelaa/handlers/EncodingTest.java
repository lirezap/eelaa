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
        var failedTransaction = new FailedTransaction(null, null);
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
