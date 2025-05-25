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
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.dromara.dynamictp.adapter.common.AbstractDtpAdapter;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.util.ReflectionUtil;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.jvmti.JVMTI;

import java.util.List;
import java.util.Objects;

/**
 * NettyDtpAdapter for managing Netty NioEventLoopGroup thread pools
 *
 * @author yanhom
 * @since 1.2.2
 */
@Slf4j
public class NettyDtpAdapter extends AbstractDtpAdapter {

    private static final String TP_PREFIX = "nettyTp";

    private static final String EXECUTOR_FIELD = "executor";

    @Override
    public void refresh(DtpProperties dtpProperties) {
        if (dtpProperties.getNettyTp() != null) {
            refresh(dtpProperties.getNettyTp(), dtpProperties.getPlatforms());
        }
    }

    @Override
    protected String getTpPrefix() {
        return TP_PREFIX;
    }

    @Override
    protected void initialize() {
        super.initialize();
        List<NioEventLoopGroup> eventLoopGroups = JVMTI.getInstances(NioEventLoopGroup.class);
        if (CollectionUtils.isEmpty(eventLoopGroups)) {
            if (log.isDebugEnabled()) {
                log.debug("Cannot find instances of NioEventLoopGroup.");
            }
            return;
        }
        
        for (val eventLoopGroup : eventLoopGroups) {
            String poolType = determinePoolType(eventLoopGroup);
            String tpName = genTpName(poolType, eventLoopGroup.hashCode());
            
            try {
                NettyEventLoopGroupProxy proxy = new NettyEventLoopGroupProxy(eventLoopGroup);
                ExecutorWrapper wrapper = new ExecutorWrapper(tpName, proxy);
                executors.put(tpName, wrapper);
                log.info("DynamicTp adapter, netty {} registered for monitoring, tpName: {}, threadCount: {}", 
                        poolType, tpName, eventLoopGroup.executorCount());
            } catch (Exception e) {
                log.error("DynamicTp adapter, netty {} registration failed, tpName: {}, cause: {}", 
                        poolType, tpName, e.getMessage());
            }
        }
    }
    
    /**
     * Determine if this is a Boss or Worker thread pool
     * 
     * @param eventLoopGroup the event loop group to check
     * @return the pool type (Boss or Worker)
     */
    private String determinePoolType(NioEventLoopGroup eventLoopGroup) {
        if (eventLoopGroup.executorCount() <= 1) {
            return "Boss";
        }
        return "Worker";
    }

    private String genTpName(String poolType, int hashCode) {
        return TP_PREFIX + "#" + poolType + "#" + hashCode;
    }
}
