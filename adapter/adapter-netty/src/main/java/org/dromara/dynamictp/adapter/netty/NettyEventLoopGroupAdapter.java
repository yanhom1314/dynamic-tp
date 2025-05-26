/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.dynamictp.adapter.netty;

import io.netty.channel.nio.NioEventLoopGroup;
import org.dromara.dynamictp.core.support.adapter.ExecutorAdapter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * NettyEventLoopGroupAdapter implements ExecutorAdapter, the goal of this class
 * is to be compatible with {@link io.netty.channel.nio.NioEventLoopGroup}.
 *
 * @author yanhom
 * @since 1.2.2
 */
public class NettyEventLoopGroupAdapter implements ExecutorAdapter<NioEventLoopGroup> {

    private final NioEventLoopGroup executor;

    public NettyEventLoopGroupAdapter(NioEventLoopGroup executor) {
        this.executor = executor;
    }

    @Override
    public NioEventLoopGroup getOriginal() {
        return this.executor;
    }

    @Override
    public int getCorePoolSize() {
        return this.executor.executorCount();
    }

    @Override
    public void setCorePoolSize(int corePoolSize) {
    }

    @Override
    public int getMaximumPoolSize() {
        return this.executor.executorCount();
    }

    @Override
    public void setMaximumPoolSize(int maximumPoolSize) {
    }

    @Override
    public int getPoolSize() {
        return this.executor.executorCount();
    }

    @Override
    public int getActiveCount() {
        return -1;
    }

    @Override
    public boolean isShutdown() {
        return this.executor.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return this.executor.isTerminated();
    }

    @Override
    public boolean isTerminating() {
        return this.executor.isShuttingDown();
    }
}
