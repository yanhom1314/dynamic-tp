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

package org.dromara.dynamictp.test.core.system;

import org.dromara.dynamictp.core.system.CpuMetricsCaptor;
import org.dromara.dynamictp.core.system.MemoryMetricsCaptor;
import org.dromara.dynamictp.core.system.OperatingSystemBeanManager;
import org.dromara.dynamictp.core.system.SystemMetricManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SystemMetricManagerTest related
 *
 * @author devin
 * @since 1.1.8
 */
@ExtendWith(MockitoExtension.class)
class SystemMetricManagerTest {

    private OperatingSystemMXBean mockOsBean;
    private CpuMetricsCaptor mockCpuCaptor;
    private MemoryMetricsCaptor mockMemoryCaptor;
    private ScheduledExecutorService mockExecutor;

    @BeforeEach
    void setUp() {
        mockOsBean = mock(OperatingSystemMXBean.class);
        mockCpuCaptor = mock(CpuMetricsCaptor.class);
        mockMemoryCaptor = mock(MemoryMetricsCaptor.class);
        mockExecutor = mock(ScheduledExecutorService.class);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testGetSystemMetric() {
        try (MockedStatic<ManagementFactory> mockedManagementFactory = Mockito.mockStatic(ManagementFactory.class);
             MockedStatic<OperatingSystemBeanManager> mockedOsBeanManager = Mockito.mockStatic(OperatingSystemBeanManager.class);
             MockedStatic<SystemMetricManager> mockedSystemMetricManager = Mockito.mockStatic(SystemMetricManager.class, 
                     Mockito.CALLS_REAL_METHODS)) {
            
            when(mockOsBean.getSystemLoadAverage()).thenReturn(1.5);
            when(mockOsBean.getAvailableProcessors()).thenReturn(8);
            mockedManagementFactory.when(() -> ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class))
                    .thenReturn(mockOsBean);
            
            mockedOsBeanManager.when(OperatingSystemBeanManager::getSystemCpuUsage).thenReturn(0.75);
            
            mockedSystemMetricManager.when(SystemMetricManager::getProcessCpuUsage).thenReturn(0.5);
            mockedSystemMetricManager.when(SystemMetricManager::getLongLivedMemoryUsage).thenReturn(0.6);
            
            String systemMetric = SystemMetricManager.getSystemMetric();
            
            assertNotNull(systemMetric);
            assertTrue(systemMetric.contains("sAvgLoad=1.50"));
            assertTrue(systemMetric.contains("sCpuUsage=0.75"));
            assertTrue(systemMetric.contains("pCpuUsage=0.50"));
            assertTrue(systemMetric.contains("cpuCores=8"));
            assertTrue(systemMetric.contains("oldMemUsage=0.60"));
        }
    }

    @Test
    void testGetProcessCpuUsage() {
        try (MockedStatic<SystemMetricManager> mockedSystemMetricManager = Mockito.mockStatic(SystemMetricManager.class, 
                Mockito.CALLS_REAL_METHODS)) {
            
            java.lang.reflect.Field cpuCaptorField = SystemMetricManager.class.getDeclaredField("CPU_METRICS_CAPTOR");
            cpuCaptorField.setAccessible(true);
            Object originalCpuCaptor = cpuCaptorField.get(null);
            
            try {
                cpuCaptorField.set(null, mockCpuCaptor);
                
                when(mockCpuCaptor.getProcessCpuUsage()).thenReturn(0.75);
                
                double result = SystemMetricManager.getProcessCpuUsage();
                
                assertEquals(0.75, result);
                verify(mockCpuCaptor).getProcessCpuUsage();
            } finally {
                cpuCaptorField.set(null, originalCpuCaptor);
            }
        } catch (Exception e) {
            throw new RuntimeException("Test failed", e);
        }
    }

    @Test
    void testGetLongLivedMemoryUsage() {
        try (MockedStatic<SystemMetricManager> mockedSystemMetricManager = Mockito.mockStatic(SystemMetricManager.class, 
                Mockito.CALLS_REAL_METHODS)) {
            
            java.lang.reflect.Field memoryCaptorField = SystemMetricManager.class.getDeclaredField("MEMORY_METRICS_CAPTOR");
            memoryCaptorField.setAccessible(true);
            Object originalMemoryCaptor = memoryCaptorField.get(null);
            
            try {
                memoryCaptorField.set(null, mockMemoryCaptor);
                
                when(mockMemoryCaptor.getLongLivedMemoryUsage()).thenReturn(0.85);
                
                double result = SystemMetricManager.getLongLivedMemoryUsage();
                
                assertEquals(0.85, result);
                verify(mockMemoryCaptor).getLongLivedMemoryUsage();
            } finally {
                memoryCaptorField.set(null, originalMemoryCaptor);
            }
        } catch (Exception e) {
            throw new RuntimeException("Test failed", e);
        }
    }

    @Test
    void testDestroy() {
        try (MockedStatic<SystemMetricManager> mockedSystemMetricManager = Mockito.mockStatic(SystemMetricManager.class, 
                Mockito.CALLS_REAL_METHODS)) {
            
            java.lang.reflect.Field executorField = SystemMetricManager.class.getDeclaredField("EXECUTOR");
            executorField.setAccessible(true);
            Object originalExecutor = executorField.get(null);
            
            try {
                executorField.set(null, mockExecutor);
                
                SystemMetricManager.destroy();
                
                verify(mockExecutor).shutdown();
            } finally {
                executorField.set(null, originalExecutor);
            }
        } catch (Exception e) {
            throw new RuntimeException("Test failed", e);
        }
    }
}
