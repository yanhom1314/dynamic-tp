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

package org.dromara.dynamictp.test.core.handler;

import org.dromara.dynamictp.common.entity.ThreadPoolStats;
import org.dromara.dynamictp.common.util.ExtensionServiceLoader;
import org.dromara.dynamictp.core.handler.CollectorHandler;
import org.dromara.dynamictp.core.monitor.collector.LogCollector;
import org.dromara.dynamictp.core.monitor.collector.MetricsCollector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * CollectorHandlerTest related
 *
 * @author devin
 * @since 1.1.8
 */
@ExtendWith(MockitoExtension.class)
class CollectorHandlerTest {

    private CollectorHandler collectorHandler;
    private ThreadPoolStats mockStats;
    private MetricsCollector mockCollector1;
    private MetricsCollector mockCollector2;

    @BeforeEach
    void setUp() {
        mockStats = mock(ThreadPoolStats.class);
        mockCollector1 = mock(MetricsCollector.class);
        mockCollector2 = mock(MetricsCollector.class);
        
        when(mockCollector1.type()).thenReturn("type1");
        when(mockCollector2.type()).thenReturn("type2");
    }

    @Test
    void testGetInstance() {
        CollectorHandler instance1 = CollectorHandler.getInstance();
        CollectorHandler instance2 = CollectorHandler.getInstance();
        
        assertNotNull(instance1);
        assertNotNull(instance2);
        assert(instance1 == instance2);
    }

    @Test
    void testCollectWithMatchingTypes() throws Exception {
        try (MockedStatic<ExtensionServiceLoader> mockedLoader = Mockito.mockStatic(ExtensionServiceLoader.class)) {
            List<MetricsCollector> collectors = new ArrayList<>();
            mockedLoader.when(() -> ExtensionServiceLoader.get(MetricsCollector.class))
                    .thenReturn(collectors);
            
            collectorHandler = new CollectorHandler();
            
            Field collectorsField = CollectorHandler.class.getDeclaredField("COLLECTORS");
            collectorsField.setAccessible(true);
            Map<String, MetricsCollector> collectorsMap = (Map<String, MetricsCollector>) collectorsField.get(null);
            collectorsMap.put("type1", mockCollector1);
            collectorsMap.put("type2", mockCollector2);
            
            List<String> types = Arrays.asList("type1", "type2");
            collectorHandler.collect(mockStats, types);
            
            verify(mockCollector1, times(1)).collect(mockStats);
            verify(mockCollector2, times(1)).collect(mockStats);
        }
    }

    @Test
    void testCollectWithNonMatchingTypes() throws Exception {
        try (MockedStatic<ExtensionServiceLoader> mockedLoader = Mockito.mockStatic(ExtensionServiceLoader.class)) {
            List<MetricsCollector> collectors = new ArrayList<>();
            mockedLoader.when(() -> ExtensionServiceLoader.get(MetricsCollector.class))
                    .thenReturn(collectors);
            
            collectorHandler = new CollectorHandler();
            
            Field collectorsField = CollectorHandler.class.getDeclaredField("COLLECTORS");
            collectorsField.setAccessible(true);
            Map<String, MetricsCollector> collectorsMap = (Map<String, MetricsCollector>) collectorsField.get(null);
            collectorsMap.put("type1", mockCollector1);
            collectorsMap.put("type2", mockCollector2);
            
            List<String> types = Arrays.asList("type3", "type4");
            collectorHandler.collect(mockStats, types);
            
            verify(mockCollector1, never()).collect(any());
            verify(mockCollector2, never()).collect(any());
        }
    }

    @Test
    void testCollectWithNullOrEmptyParams() throws Exception {
        try (MockedStatic<ExtensionServiceLoader> mockedLoader = Mockito.mockStatic(ExtensionServiceLoader.class)) {
            List<MetricsCollector> collectors = new ArrayList<>();
            mockedLoader.when(() -> ExtensionServiceLoader.get(MetricsCollector.class))
                    .thenReturn(collectors);
            
            collectorHandler = new CollectorHandler();
            
            Field collectorsField = CollectorHandler.class.getDeclaredField("COLLECTORS");
            collectorsField.setAccessible(true);
            Map<String, MetricsCollector> collectorsMap = (Map<String, MetricsCollector>) collectorsField.get(null);
            collectorsMap.put("type1", mockCollector1);
            
            List<String> types = Arrays.asList("type1");
            collectorHandler.collect(null, types);
            
            collectorHandler.collect(mockStats, new ArrayList<>());
            
            collectorHandler.collect(mockStats, null);
            
            verify(mockCollector1, never()).collect(any());
        }
    }

    @Test
    void testCollectorInitialization() throws Exception {
        try (MockedStatic<ExtensionServiceLoader> mockedLoader = Mockito.mockStatic(ExtensionServiceLoader.class)) {
            List<MetricsCollector> collectors = new ArrayList<>();
            collectors.add(mockCollector1);
            mockedLoader.when(() -> ExtensionServiceLoader.get(MetricsCollector.class))
                    .thenReturn(collectors);
            
            collectorHandler = new CollectorHandler();
            
            Field collectorsField = CollectorHandler.class.getDeclaredField("COLLECTORS");
            collectorsField.setAccessible(true);
            Map<String, MetricsCollector> collectorsMap = (Map<String, MetricsCollector>) collectorsField.get(null);
            
            assert(collectorsMap.containsKey("type1"));
            assert(collectorsMap.get("type1") == mockCollector1);
            
            assert(collectorsMap.containsValue(collectorsMap.get("logging")));
            assert(collectorsMap.containsValue(collectorsMap.get("micrometer")));
            assert(collectorsMap.containsValue(collectorsMap.get("internal_logging")));
            assert(collectorsMap.containsValue(collectorsMap.get("jmx")));
        }
    }
}
