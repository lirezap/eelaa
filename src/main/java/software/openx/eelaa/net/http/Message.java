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
package software.openx.eelaa.net.http;

/**
 * Base HTTP message definition.
 *
 * @param <D> data type
 * @author Alireza Pourtaghi
 */
public class Message<D> {
    private int sequenceId;
    private long ts;
    private D data;

    public Message() {
    }

    public int getSequenceId() {
        return sequenceId;
    }

    public long getTs() {
        return ts;
    }

    public D getData() {
        return data;
    }

    @Override
    public String toString() {
        return "Message{" +
                "sequenceId=" + sequenceId +
                ", ts=" + ts +
                ", data=" + data +
                '}';
    }
}
