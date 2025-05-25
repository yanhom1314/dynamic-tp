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

import org.dromara.dynamictp.common.util.MethodUtil;
import org.dromara.dynamictp.core.system.OperatingSystemBeanManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * OperatingSystemBeanManagerTest related
 *
 * @author devin
 * @since 1.1.8
 */
@ExtendWith(MockitoExtension.class)
class OperatingSystemBeanManagerTest {

    @Test
    void testGetOperatingSystemBean() {
        try (MockedStatic<ManagementFactory> mockedManagementFactory = Mockito.mockStatic(ManagementFactory.class)) {
            OperatingSystemMXBean mockBean = mock(OperatingSystemMXBean.class);
            mockedManagementFactory.when(() -> ManagementFactory.getOperatingSystemMXBean())
                    .thenReturn(mockBean);
            
            java.lang.reflect.Field field = OperatingSystemBeanManager.class.getDeclaredField("OPERATING_SYSTEM_BEAN");
            field.setAccessible(true);
            Object originalBean = field.get(null);
            
            try {
                field.set(null, mockBean);
                
                OperatingSystemMXBean result = OperatingSystemBeanManager.getOperatingSystemBean();
                
                assertEquals(mockBean, result);
            } finally {
                field.set(null, originalBean);
            }
        } catch (Exception e) {
            throw new RuntimeException("Test failed", e);
        }
    }

    @Test
    void testGetSystemCpuUsage() {
        try (MockedStatic<MethodUtil> mockedMethodUtil = Mockito.mockStatic(MethodUtil.class)) {
            mockedMethodUtil.when(() -> MethodUtil.invokeAndReturnDouble(any(Method.class), any()))
                    .thenReturn(0.75);
            
            double result = OperatingSystemBeanManager.getSystemCpuUsage();
            
            assertEquals(0.75, result);
            mockedMethodUtil.verify(() -> MethodUtil.invokeAndReturnDouble(any(Method.class), any()));
        }
    }

    @Test
    void testGetProcessCpuTime() {
        try (MockedStatic<MethodUtil> mockedMethodUtil = Mockito.mockStatic(MethodUtil.class)) {
            mockedMethodUtil.when(() -> MethodUtil.invokeAndReturnLong(any(Method.class), any()))
                    .thenReturn(1000000000L);
            
            long result = OperatingSystemBeanManager.getProcessCpuTime();
            
            assertEquals(1000000000L, result);
            mockedMethodUtil.verify(() -> MethodUtil.invokeAndReturnLong(any(Method.class), any()));
        }
    }

    @Test
    void testGetTotalPhysicalMem() {
        try (MockedStatic<MethodUtil> mockedMethodUtil = Mockito.mockStatic(MethodUtil.class)) {
            mockedMethodUtil.when(() -> MethodUtil.invokeAndReturnLong(any(Method.class), any()))
                    .thenReturn(8589934592L); // 8GB
            
            long result = OperatingSystemBeanManager.getTotalPhysicalMem();
            
            assertEquals(8589934592L, result);
            mockedMethodUtil.verify(() -> MethodUtil.invokeAndReturnLong(any(Method.class), any()));
        }
    }

    @Test
    void testGetFreePhysicalMem() {
        try (MockedStatic<MethodUtil> mockedMethodUtil = Mockito.mockStatic(MethodUtil.class)) {
            mockedMethodUtil.when(() -> MethodUtil.invokeAndReturnLong(any(Method.class), any()))
                    .thenReturn(4294967296L); // 4GB
            
            long result = OperatingSystemBeanManager.getFreePhysicalMem();
            
            assertEquals(4294967296L, result);
            mockedMethodUtil.verify(() -> MethodUtil.invokeAndReturnLong(any(Method.class), any()));
        }
    }
}
