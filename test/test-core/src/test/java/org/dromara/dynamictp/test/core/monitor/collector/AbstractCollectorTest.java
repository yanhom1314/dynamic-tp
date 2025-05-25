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

import org.dromara.dynamictp.common.entity.ThreadPoolStats;
import org.dromara.dynamictp.core.monitor.collector.AbstractCollector;
import org.dromara.dynamictp.core.monitor.collector.MetricsCollector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AbstractCollectorTest related
 *
 * @author devin
 * @since 1.1.8
 */
class AbstractCollectorTest {

    private TestCollector testCollector;

    @BeforeEach
    void setUp() {
        testCollector = new TestCollector();
    }

    @Test
    void testSupportWithMatchingType() {
        assertTrue(testCollector.support("test"));
        assertTrue(testCollector.support("TEST"));
        assertTrue(testCollector.support("Test"));
    }

    @Test
    void testSupportWithNonMatchingType() {
        assertFalse(testCollector.support("other"));
        assertFalse(testCollector.support(""));
        assertFalse(testCollector.support(null));
    }

    /**
     * Test implementation of AbstractCollector
     */
    private static class TestCollector extends AbstractCollector {
        @Override
        public void collect(ThreadPoolStats poolStats) {
        }

        @Override
        public String type() {
            return "test";
        }
    }
}
