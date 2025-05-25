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
import org.dromara.dynamictp.core.system.OperatingSystemBeanManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * CpuMetricsCaptorTest related
 *
 * @author devin
 * @since 1.1.8
 */
@ExtendWith(MockitoExtension.class)
class CpuMetricsCaptorTest {

    private CpuMetricsCaptor cpuMetricsCaptor;
    private RuntimeMXBean mockRuntimeBean;

    @BeforeEach
    void setUp() {
        cpuMetricsCaptor = new CpuMetricsCaptor();
        mockRuntimeBean = mock(RuntimeMXBean.class);
    }

    @Test
    void testGetProcessCpuUsage() {
        try {
            Field field = CpuMetricsCaptor.class.getDeclaredField("currProcessCpuUsage");
            field.setAccessible(true);
            field.set(cpuMetricsCaptor, 0.75);
            
            assertEquals(0.75, cpuMetricsCaptor.getProcessCpuUsage());
        } catch (Exception e) {
            throw new RuntimeException("Test failed", e);
        }
    }

    @Test
    void testRun() {
        try (MockedStatic<ManagementFactory> mockedManagementFactory = Mockito.mockStatic(ManagementFactory.class);
             MockedStatic<OperatingSystemBeanManager> mockedOsBeanManager = Mockito.mockStatic(OperatingSystemBeanManager.class)) {
            
            OperatingSystemMXBean mockOsBean = mock(OperatingSystemMXBean.class);
            when(mockOsBean.getAvailableProcessors()).thenReturn(8); // Fixed processor count for consistent test
            mockedManagementFactory.when(() -> ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class))
                    .thenReturn(mockOsBean);
            
            Field prevProcessCpuTimeField = CpuMetricsCaptor.class.getDeclaredField("prevProcessCpuTime");
            prevProcessCpuTimeField.setAccessible(true);
            prevProcessCpuTimeField.set(cpuMetricsCaptor, 1000000000L); // 1 second in nanoseconds
            
            Field prevUpTimeField = CpuMetricsCaptor.class.getDeclaredField("prevUpTime");
            prevUpTimeField.setAccessible(true);
            prevUpTimeField.set(cpuMetricsCaptor, 1000L); // 1 second in milliseconds
            
            mockedManagementFactory.when(() -> ManagementFactory.getPlatformMXBean(RuntimeMXBean.class))
                    .thenReturn(mockRuntimeBean);
            
            mockedOsBeanManager.when(OperatingSystemBeanManager::getProcessCpuTime)
                    .thenReturn(3000000000L); // 3 seconds in nanoseconds
            
            when(mockRuntimeBean.getUptime()).thenReturn(3000L); // 3 seconds in milliseconds
            
            cpuMetricsCaptor.run();
            
            Field currProcessCpuUsageField = CpuMetricsCaptor.class.getDeclaredField("currProcessCpuUsage");
            currProcessCpuUsageField.setAccessible(true);
            double result = (double) currProcessCpuUsageField.get(cpuMetricsCaptor);
            
            assertEquals(0.125, result, 0.001);
            
            assertEquals(3000000000L, prevProcessCpuTimeField.get(cpuMetricsCaptor));
            assertEquals(3000L, prevUpTimeField.get(cpuMetricsCaptor));
        } catch (Exception e) {
            throw new RuntimeException("Test failed", e);
        }
    }

    @Test
    void testRunWithException() {
        try (MockedStatic<ManagementFactory> mockedManagementFactory = Mockito.mockStatic(ManagementFactory.class)) {
            
            mockedManagementFactory.when(() -> ManagementFactory.getPlatformMXBean(RuntimeMXBean.class))
                    .thenThrow(new RuntimeException("Test exception"));
            
            cpuMetricsCaptor.run();
            
            assertEquals(-1, cpuMetricsCaptor.getProcessCpuUsage());
        }
    }
}
