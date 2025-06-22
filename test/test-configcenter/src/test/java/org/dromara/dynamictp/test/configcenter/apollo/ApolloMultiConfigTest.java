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

package org.dromara.dynamictp.test.configcenter.apollo;

import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.internals.YamlConfigFile;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.test.configcenter.DtpBaseTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Properties;
import java.util.concurrent.ThreadPoolExecutor;

import static com.ctrip.framework.apollo.core.ConfigConsts.CONFIG_FILE_CONTENT_KEY;

/**
 * ApolloMultiConfigTest for testing multi-configuration file scenarios
 *
 * @author yanhom
 * @since 1.0.0
 */
class ApolloMultiConfigTest extends DtpBaseTest {

    @Test
    void testMultiConfigRefresh() throws InterruptedException {
        ThreadPoolExecutor executor1 = context.getBean("dtpExecutor1", ThreadPoolExecutor.class);
        ThreadPoolExecutor executor2 = context.getBean("dtpExecutor2", ThreadPoolExecutor.class);
        
        int initialCoreSize1 = executor1.getCorePoolSize();
        int initialCoreSize2 = executor2.getCorePoolSize();
        
        System.out.println("Initial corePoolSize1: " + initialCoreSize1);
        System.out.println("Initial corePoolSize2: " + initialCoreSize2);
        
        mockCommonConfigChange();
        Thread.sleep(2000L);
        
        int updatedCoreSize1 = executor1.getCorePoolSize();
        int preservedCoreSize2 = executor2.getCorePoolSize();
        
        System.out.println("After common config change - corePoolSize1: " + updatedCoreSize1);
        System.out.println("After common config change - corePoolSize2: " + preservedCoreSize2);
        
        Assertions.assertNotEquals(initialCoreSize1, updatedCoreSize1, "Executor1 should be updated");
        Assertions.assertEquals(initialCoreSize2, preservedCoreSize2, "Executor2 should be preserved");
        
        mockProjectConfigChange();
        Thread.sleep(2000L);
        
        int preservedCoreSize1 = executor1.getCorePoolSize();
        int updatedCoreSize2 = executor2.getCorePoolSize();
        
        System.out.println("After project config change - corePoolSize1: " + preservedCoreSize1);
        System.out.println("After project config change - corePoolSize2: " + updatedCoreSize2);
        
        Assertions.assertEquals(updatedCoreSize1, preservedCoreSize1, "Executor1 should be preserved");
        Assertions.assertNotEquals(preservedCoreSize2, updatedCoreSize2, "Executor2 should be updated");
    }

    private void mockCommonConfigChange() {
        YamlConfigFile configFile = (YamlConfigFile) ConfigService.getConfigFile("common-config", ConfigFileFormat.YML);
        Properties newProperties = new Properties();
        String content =
                "dynamictp:\n" +
                "  enabled: true\n" +
                "  executors:\n" +
                "    - threadPoolName: dtpExecutor1\n" +
                "      threadPoolAliasName: 通用线程池\n" +
                "      executorType: common\n" +
                "      corePoolSize: 15\n" +
                "      maximumPoolSize: 30\n";
        newProperties.setProperty(CONFIG_FILE_CONTENT_KEY, content);
        configFile.onRepositoryChange("common-config.yml", newProperties);
    }

    private void mockProjectConfigChange() {
        YamlConfigFile configFile = (YamlConfigFile) ConfigService.getConfigFile("project-config", ConfigFileFormat.YML);
        Properties newProperties = new Properties();
        String content =
                "dynamictp:\n" +
                "  enabled: true\n" +
                "  executors:\n" +
                "    - threadPoolName: dtpExecutor2\n" +
                "      threadPoolAliasName: 项目线程池\n" +
                "      executorType: common\n" +
                "      corePoolSize: 25\n" +
                "      maximumPoolSize: 50\n";
        newProperties.setProperty(CONFIG_FILE_CONTENT_KEY, content);
        configFile.onRepositoryChange("project-config.yml", newProperties);
    }

    @Test
    void testConfigurationMerging() {
        DtpProperties dtpProperties = DtpProperties.getInstance();
        
        Assertions.assertNotNull(dtpProperties.getExecutors(), "Executors should not be null");
        Assertions.assertTrue(dtpProperties.getExecutors().size() >= 2, "Should have at least 2 executors from different config sources");
        
        boolean hasExecutor1 = dtpProperties.getExecutors().stream()
                .anyMatch(executor -> "dtpExecutor1".equals(executor.getThreadPoolName()));
        boolean hasExecutor2 = dtpProperties.getExecutors().stream()
                .anyMatch(executor -> "dtpExecutor2".equals(executor.getThreadPoolName()));
        
        Assertions.assertTrue(hasExecutor1, "Should have dtpExecutor1 from common config");
        Assertions.assertTrue(hasExecutor2, "Should have dtpExecutor2 from project config");
    }
}
