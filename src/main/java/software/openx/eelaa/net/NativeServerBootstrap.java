/*
 * Copyright 2026 lirezap@protonmail.com
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
package software.openx.eelaa.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollIoHandler;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueChannelOption;
import io.netty.channel.kqueue.KQueueIoHandler;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.agrona.SystemUtil;

/**
 * Native configured server bootstrap.
 *
 * @author Alireza Pourtaghi
 */
final class NativeServerBootstrap extends ServerBootstrap {
    private final TCPServerConfig config;

    private NativeServerBootstrap(final TCPServerConfig config) {
        this.config = config;
    }

    public static NativeServerBootstrap newInstance(final TCPServerConfig config) {
        return new NativeServerBootstrap(config);
    }

    public void configure() {
        // TODO: Use all appropriate server/client side channel options.
        this.group(bossGroup(), workerGroup());
        this.channel(serverSocketChannel());

        if (Epoll.isAvailable()) {
            this.option(EpollChannelOption.SO_BACKLOG, config.getSoBacklog());
            this.option(EpollChannelOption.SO_REUSEADDR, true);
            this.option(EpollChannelOption.SO_REUSEPORT, true);
        } else if (KQueue.isAvailable()) {
            this.option(KQueueChannelOption.SO_BACKLOG, config.getSoBacklog());
            this.option(KQueueChannelOption.SO_REUSEADDR, true);
            this.option(KQueueChannelOption.SO_REUSEPORT, true);
        } else {
            this.option(ChannelOption.SO_BACKLOG, config.getSoBacklog());
            this.option(ChannelOption.SO_REUSEADDR, true);
        }

        this.childOption(ChannelOption.TCP_NODELAY, true);
        this.childOption(ChannelOption.SO_KEEPALIVE, true);
        this.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        this.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, writeBufferWaterMark());
    }

    private EventLoopGroup bossGroup() {
        if (SystemUtil.isLinux()) {
            return new MultiThreadIoEventLoopGroup(1, EpollIoHandler.newFactory());
        } else if (SystemUtil.isMac()) {
            return new MultiThreadIoEventLoopGroup(1, KQueueIoHandler.newFactory());
        } else {
            return new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
        }
    }

    private EventLoopGroup workerGroup() {
        if (SystemUtil.isLinux()) {
            return new MultiThreadIoEventLoopGroup(config.getEventLoopGroupNThreads(), EpollIoHandler.newFactory());
        } else if (SystemUtil.isMac()) {
            return new MultiThreadIoEventLoopGroup(config.getEventLoopGroupNThreads(), KQueueIoHandler.newFactory());
        } else {
            return new MultiThreadIoEventLoopGroup(config.getEventLoopGroupNThreads(), NioIoHandler.newFactory());
        }
    }

    private Class<? extends ServerChannel> serverSocketChannel() {
        if (SystemUtil.isLinux()) {
            return EpollServerSocketChannel.class;
        } else if (SystemUtil.isMac()) {
            return KQueueServerSocketChannel.class;
        } else {
            return NioServerSocketChannel.class;
        }
    }

    private WriteBufferWaterMark writeBufferWaterMark() {
        return new WriteBufferWaterMark(config.getLowWriteBufferWaterMark(), config.getHighWriteBufferWaterMark());
    }
}
