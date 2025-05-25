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

import org.dromara.dynamictp.core.system.MemoryMetricsCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * MemoryMetricsCaptorTest related
 *
 * @author devin
 * @since 1.1.8
 */
@ExtendWith(MockitoExtension.class)
class MemoryMetricsCaptorTest {

    private MemoryMetricsCaptor memoryMetricsCaptor;

    @BeforeEach
    void setUp() {
        memoryMetricsCaptor = new MemoryMetricsCaptor();
    }

    @Test
    void testGetLongLivedMemoryUsageWhenNotInitialized() {
        assertEquals(-1, memoryMetricsCaptor.getLongLivedMemoryUsage());
    }

    @Test
    void testGetLongLivedMemoryUsageWhenInitialized() {
        try {
            Field maxField = MemoryMetricsCaptor.class.getDeclaredField("max");
            maxField.setAccessible(true);
            maxField.set(memoryMetricsCaptor, 1000.0);
            
            Field usedField = MemoryMetricsCaptor.class.getDeclaredField("used");
            usedField.setAccessible(true);
            usedField.set(memoryMetricsCaptor, 750.0);
            
            assertEquals(0.75, memoryMetricsCaptor.getLongLivedMemoryUsage());
        } catch (Exception e) {
            throw new RuntimeException("Test failed", e);
        }
    }

    @Test
    void testRunWithOldGenPool() {
        try (MockedStatic<ManagementFactory> mockedManagementFactory = Mockito.mockStatic(ManagementFactory.class)) {
            List<MemoryPoolMXBean> memoryPoolBeans = new ArrayList<>();
            MemoryPoolMXBean oldGenBean = mock(MemoryPoolMXBean.class);
            when(oldGenBean.getName()).thenReturn("PS Old Gen");
            
            MemoryUsage memoryUsage = mock(MemoryUsage.class);
            when(memoryUsage.getUsed()).thenReturn(750L);
            when(memoryUsage.getMax()).thenReturn(1000L);
            when(oldGenBean.getUsage()).thenReturn(memoryUsage);
            
            memoryPoolBeans.add(oldGenBean);
            
            mockedManagementFactory.when(() -> ManagementFactory.getPlatformMXBeans(MemoryPoolMXBean.class))
                    .thenReturn(memoryPoolBeans);
            
            memoryMetricsCaptor.run();
            
            Field maxField = MemoryMetricsCaptor.class.getDeclaredField("max");
            maxField.setAccessible(true);
            double maxValue = (double) maxField.get(memoryMetricsCaptor);
            
            Field usedField = MemoryMetricsCaptor.class.getDeclaredField("used");
            usedField.setAccessible(true);
            double usedValue = (double) usedField.get(memoryMetricsCaptor);
            
            assertEquals(1000.0, maxValue);
            assertEquals(750.0, usedValue);
            assertEquals(0.75, memoryMetricsCaptor.getLongLivedMemoryUsage());
        } catch (Exception e) {
            throw new RuntimeException("Test failed", e);
        }
    }

    @Test
    void testRunWithTenuredGenPool() {
        try (MockedStatic<ManagementFactory> mockedManagementFactory = Mockito.mockStatic(ManagementFactory.class)) {
            List<MemoryPoolMXBean> memoryPoolBeans = new ArrayList<>();
            MemoryPoolMXBean tenuredGenBean = mock(MemoryPoolMXBean.class);
            when(tenuredGenBean.getName()).thenReturn("CMS Tenured Gen");
            
            MemoryUsage memoryUsage = mock(MemoryUsage.class);
            when(memoryUsage.getUsed()).thenReturn(600L);
            when(memoryUsage.getMax()).thenReturn(800L);
            when(tenuredGenBean.getUsage()).thenReturn(memoryUsage);
            
            memoryPoolBeans.add(tenuredGenBean);
            
            mockedManagementFactory.when(() -> ManagementFactory.getPlatformMXBeans(MemoryPoolMXBean.class))
                    .thenReturn(memoryPoolBeans);
            
            memoryMetricsCaptor.run();
            
            assertEquals(0.75, memoryMetricsCaptor.getLongLivedMemoryUsage());
        } catch (Exception e) {
            throw new RuntimeException("Test failed", e);
        }
    }

    @Test
    void testRunWithNoLongLivedPool() {
        try (MockedStatic<ManagementFactory> mockedManagementFactory = Mockito.mockStatic(ManagementFactory.class)) {
            List<MemoryPoolMXBean> memoryPoolBeans = new ArrayList<>();
            MemoryPoolMXBean edenBean = mock(MemoryPoolMXBean.class);
            when(edenBean.getName()).thenReturn("PS Eden Space");
            
            memoryPoolBeans.add(edenBean);
            
            mockedManagementFactory.when(() -> ManagementFactory.getPlatformMXBeans(MemoryPoolMXBean.class))
                    .thenReturn(memoryPoolBeans);
            
            Field maxField = MemoryMetricsCaptor.class.getDeclaredField("max");
            maxField.setAccessible(true);
            maxField.set(memoryMetricsCaptor, 1000.0);
            
            Field usedField = MemoryMetricsCaptor.class.getDeclaredField("used");
            usedField.setAccessible(true);
            usedField.set(memoryMetricsCaptor, 750.0);
            
            memoryMetricsCaptor.run();
            
            assertEquals(1000.0, (double) maxField.get(memoryMetricsCaptor));
            assertEquals(750.0, (double) usedField.get(memoryMetricsCaptor));
            assertEquals(0.75, memoryMetricsCaptor.getLongLivedMemoryUsage());
        } catch (Exception e) {
            throw new RuntimeException("Test failed", e);
        }
    }

    @Test
    void testRunWithException() {
        try (MockedStatic<ManagementFactory> mockedManagementFactory = Mockito.mockStatic(ManagementFactory.class)) {
            mockedManagementFactory.when(() -> ManagementFactory.getPlatformMXBeans(MemoryPoolMXBean.class))
                    .thenThrow(new RuntimeException("Test exception"));
            
            memoryMetricsCaptor.run();
            
            assertEquals(-1, memoryMetricsCaptor.getLongLivedMemoryUsage());
        }
    }
}
