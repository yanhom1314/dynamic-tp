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

package org.dromara.dynamictp.test.core.monitor.collector;

import org.dromara.dynamictp.common.em.CollectorTypeEnum;
import org.dromara.dynamictp.common.entity.ThreadPoolStats;
import org.dromara.dynamictp.common.util.JsonUtil;
import org.dromara.dynamictp.core.monitor.collector.LogCollector;
import org.dromara.dynamictp.logging.LogHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * LogCollectorTest related
 *
 * @author devin
 * @since 1.1.8
 */
@ExtendWith(MockitoExtension.class)
class LogCollectorTest {

    private LogCollector logCollector;
    private ThreadPoolStats mockStats;
    private Logger mockLogger;

    @BeforeEach
    void setUp() {
        logCollector = new LogCollector();
        mockStats = mock(ThreadPoolStats.class);
        mockLogger = mock(Logger.class);
    }

    @Test
    void testCollectWithValidLogger() {
        try (MockedStatic<LogHelper> mockedLogHelper = Mockito.mockStatic(LogHelper.class);
             MockedStatic<JsonUtil> mockedJsonUtil = Mockito.mockStatic(JsonUtil.class)) {
            
            mockedLogHelper.when(LogHelper::getMonitorLogger).thenReturn(mockLogger);
            
            mockedJsonUtil.when(() -> JsonUtil.toJson(mockStats)).thenReturn("{\"poolName\":\"testPool\"}");
            
            logCollector.collect(mockStats);
            
            verify(mockLogger).info(eq("{}"), eq("{\"poolName\":\"testPool\"}"));
        }
    }

    @Test
    void testCollectWithNullLogger() {
        try (MockedStatic<LogHelper> mockedLogHelper = Mockito.mockStatic(LogHelper.class)) {
            mockedLogHelper.when(LogHelper::getMonitorLogger).thenReturn(null);
            
            logCollector.collect(mockStats);
            
        }
    }

    @Test
    void testType() {
        assertEquals(CollectorTypeEnum.LOGGING.name().toLowerCase(), logCollector.type());
    }

    @Test
    void testSupport() {
        assertEquals(true, logCollector.support("logging"));
        assertEquals(true, logCollector.support("LOGGING"));
        
        assertEquals(false, logCollector.support("other"));
    }
}
