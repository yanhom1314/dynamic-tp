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

package org.dromara.dynamictp.test.core.monitor;

import java.util.concurrent.ScheduledExecutorService;
import org.dromara.dynamictp.common.event.CollectEvent;
import org.dromara.dynamictp.common.event.AlarmCheckEvent;
import org.dromara.dynamictp.common.event.CustomContextRefreshedEvent;
import org.dromara.dynamictp.common.manager.EventBusManager;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.dromara.dynamictp.core.monitor.DtpMonitor;
import org.dromara.dynamictp.core.notifier.manager.AlarmManager;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.ThreadPoolCreator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * DtpMonitorTest related
 *
 * @author devin
 * @since 1.1.8
 */
@ExtendWith(MockitoExtension.class)
class DtpMonitorTest {

    @Mock
    private DtpProperties dtpProperties;

    private DtpMonitor dtpMonitor;

    @BeforeEach
    void setUp() {
        when(dtpProperties.getMonitorInterval()).thenReturn(5);
        when(dtpProperties.isEnabledCollect()).thenReturn(true);
        dtpMonitor = new DtpMonitor(dtpProperties);
    }

    @AfterEach
    void tearDown() {
        DtpMonitor.destroy();
    }

    @Test
    void testOnContextRefreshedEvent() {
        CustomContextRefreshedEvent event = new CustomContextRefreshedEvent("test");
        
        try (MockedStatic<ThreadPoolCreator> mockedThreadPoolCreator = Mockito.mockStatic(ThreadPoolCreator.class)) {
            ScheduledExecutorService mockExecutor = mock(ScheduledExecutorService.class);
            
            @SuppressWarnings("rawtypes")
            ScheduledFuture mockFuture = mock(ScheduledFuture.class);
            
            @SuppressWarnings("unchecked")
            ScheduledFuture<?> result = mockExecutor.scheduleWithFixedDelay(
                any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class));
            Mockito.when(result).thenReturn(mockFuture);
            
            mockedThreadPoolCreator.when(() -> ThreadPoolCreator.newScheduledThreadPool(anyString(), anyInt()))
                    .thenReturn(mockExecutor);
            
            dtpMonitor.onContextRefreshedEvent(event);
            
            when(dtpProperties.getMonitorInterval()).thenReturn(10);
            dtpMonitor.onContextRefreshedEvent(event);
            
            verify(mockExecutor).scheduleWithFixedDelay(any(Runnable.class), eq(0L), eq(10L), eq(TimeUnit.SECONDS));
        }
    }

    @Test
    void testRunMethod() {
        Set<String> executorNames = new HashSet<>();
        executorNames.add("testExecutor");
        
        try (MockedStatic<DtpRegistry> mockedRegistry = Mockito.mockStatic(DtpRegistry.class);
             MockedStatic<AlarmManager> mockedAlarmManager = Mockito.mockStatic(AlarmManager.class);
             MockedStatic<EventBusManager> mockedEventBusManager = Mockito.mockStatic(EventBusManager.class)) {
            
            mockedRegistry.when(DtpRegistry::getAllExecutorNames).thenReturn(executorNames);
            ExecutorWrapper mockWrapper = mock(ExecutorWrapper.class);
            mockedRegistry.when(() -> DtpRegistry.getExecutorWrapper("testExecutor")).thenReturn(mockWrapper);
            
            mockedAlarmManager.when(() -> AlarmManager.checkAndTryAlarmAsync(any(), any())).thenReturn(null);
            
            mockedEventBusManager.when(() -> EventBusManager.post(any())).thenAnswer(invocation -> null);
            
            java.lang.reflect.Method runMethod = DtpMonitor.class.getDeclaredMethod("run");
            runMethod.setAccessible(true);
            runMethod.invoke(dtpMonitor);
            
            mockedRegistry.verify(DtpRegistry::getAllExecutorNames);
            mockedRegistry.verify(() -> DtpRegistry.getExecutorWrapper("testExecutor"));
            mockedAlarmManager.verify(() -> AlarmManager.checkAndTryAlarmAsync(eq(mockWrapper), any()));
            
            mockedEventBusManager.verify(() -> EventBusManager.post(any()), times(2));
        } catch (Exception e) {
            throw new RuntimeException("Test failed", e);
        }
    }

    @Test
    void testDestroy() {
        try (MockedStatic<ThreadPoolCreator> mockedThreadPoolCreator = Mockito.mockStatic(ThreadPoolCreator.class)) {
            ScheduledExecutorService mockExecutor = mock(ScheduledExecutorService.class);
            mockedThreadPoolCreator.when(() -> ThreadPoolCreator.newScheduledThreadPool(anyString(), anyInt()))
                    .thenReturn(mockExecutor);
            
            CustomContextRefreshedEvent event = new CustomContextRefreshedEvent("test");
            dtpMonitor.onContextRefreshedEvent(event);
            
            DtpMonitor.destroy();
            
            verify(mockExecutor).shutdownNow();
        }
    }
}
