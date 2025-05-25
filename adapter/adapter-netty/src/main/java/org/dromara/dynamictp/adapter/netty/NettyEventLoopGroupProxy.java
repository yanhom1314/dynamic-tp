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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.dromara.dynamictp.core.aware.RejectHandlerAware;
import org.dromara.dynamictp.core.aware.TaskEnhanceAware;
import org.dromara.dynamictp.core.support.task.wrapper.TaskWrapper;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * NioEventLoopGroup wrapper for dynamic thread pool management
 *
 * @author yanhom
 * @since 1.2.2
 */
@Slf4j
public class NettyEventLoopGroupProxy implements TaskEnhanceAware, RejectHandlerAware, Executor {

    /**
     * Original NioEventLoopGroup instance
     */
    private final NioEventLoopGroup original;

    /**
     * Task wrappers, do sth enhanced.
     */
    private List<TaskWrapper> taskWrappers;

    /**
     * Reject handler type.
     */
    private String rejectHandlerType;

    public NettyEventLoopGroupProxy(NioEventLoopGroup original) {
        this.original = original;
        this.rejectHandlerType = "AbortPolicy"; // Default reject handler
    }

    public NioEventLoopGroup getOriginal() {
        return original;
    }

    public int getThreadCount() {
        return original.executorCount();
    }

    public boolean isShuttingDown() {
        return original.isShuttingDown();
    }

    public boolean isShutdown() {
        return original.isShutdown();
    }

    public boolean isTerminated() {
        return original.isTerminated();
    }

    @Override
    public List<TaskWrapper> getTaskWrappers() {
        return taskWrappers;
    }

    @Override
    public void setTaskWrappers(List<TaskWrapper> taskWrappers) {
        this.taskWrappers = taskWrappers;
    }

    @Override
    public String getRejectHandlerType() {
        return rejectHandlerType;
    }

    @Override
    public void setRejectHandlerType(String rejectHandlerType) {
        this.rejectHandlerType = rejectHandlerType;
    }

    @Override
    public void execute(Runnable command) {
        original.execute(getEnhancedTask(command));
    }

    public Runnable getEnhancedTask(Runnable command) {
        if (command != null && CollectionUtils.isNotEmpty(taskWrappers)) {
            for (TaskWrapper wrapper : taskWrappers) {
                command = wrapper.wrap(command);
            }
        }
        return command;
    }

    public void logCurrentState() {
        log.info("NettyEventLoopGroup state - threads: {}, shuttingDown: {}, shutdown: {}, terminated: {}", 
                getThreadCount(), isShuttingDown(), isShutdown(), isTerminated());
    }
}
