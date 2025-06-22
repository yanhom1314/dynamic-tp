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
        
        int initialCoreSize = executor1.getCorePoolSize();
        int initialMaxSize = executor1.getMaximumPoolSize();
        
        System.out.println("Initial corePoolSize: " + initialCoreSize);
        System.out.println("Initial maximumPoolSize: " + initialMaxSize);
        
        mockCommonConfigChange();
        Thread.sleep(2000L);
        
        int updatedCoreSize = executor1.getCorePoolSize();
        int preservedMaxSize = executor1.getMaximumPoolSize();
        
        System.out.println("After common config change - corePoolSize: " + updatedCoreSize);
        System.out.println("After common config change - maximumPoolSize: " + preservedMaxSize);
        
        Assertions.assertNotEquals(initialCoreSize, updatedCoreSize, "CorePoolSize should be updated by common config");
        
        mockProjectConfigChange();
        Thread.sleep(2000L);
        
        int finalCoreSize = executor1.getCorePoolSize();
        int updatedMaxSize = executor1.getMaximumPoolSize();
        
        System.out.println("After project config change - corePoolSize: " + finalCoreSize);
        System.out.println("After project config change - maximumPoolSize: " + updatedMaxSize);
        
        Assertions.assertEquals(updatedCoreSize, finalCoreSize, "CorePoolSize from common config should be preserved");
        Assertions.assertNotEquals(initialMaxSize, updatedMaxSize, "MaximumPoolSize should be updated by project config");
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
                "    - threadPoolName: dtpExecutor1\n" +
                "      threadPoolAliasName: 项目线程池\n" +
                "      executorType: common\n" +
                "      corePoolSize: 15\n" +
                "      maximumPoolSize: 50\n";
        newProperties.setProperty(CONFIG_FILE_CONTENT_KEY, content);
        configFile.onRepositoryChange("project-config.yml", newProperties);
    }

    @Test
    void testConfigurationMerging() {
        DtpProperties dtpProperties = DtpProperties.getInstance();
        
        Assertions.assertNotNull(dtpProperties.getExecutors(), "Executors should not be null");
        Assertions.assertTrue(dtpProperties.getExecutors().size() >= 1, "Should have at least 1 executor configured");
        
        boolean hasExecutor1 = dtpProperties.getExecutors().stream()
                .anyMatch(executor -> "dtpExecutor1".equals(executor.getThreadPoolName()));
        
        Assertions.assertTrue(hasExecutor1, "Should have dtpExecutor1 configured");
        
        Assertions.assertTrue(dtpProperties.getExecutors().stream()
                .anyMatch(executor -> executor.getCorePoolSize() > 0), "Should have valid core pool size");
        Assertions.assertTrue(dtpProperties.getExecutors().stream()
                .anyMatch(executor -> executor.getMaximumPoolSize() > 0), "Should have valid maximum pool size");
        
        System.out.println("Configuration merging test - dtpExecutor1 found: " + hasExecutor1);
        System.out.println("Total executors configured: " + dtpProperties.getExecutors().size());
    }
}
