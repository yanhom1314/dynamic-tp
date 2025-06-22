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

package org.dromara.dynamictp.core.support;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.dromara.dynamictp.common.entity.DtpExecutorProps;
import org.dromara.dynamictp.common.entity.TpExecutorProps;
import org.dromara.dynamictp.common.properties.DtpProperties;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ConfigMergeHelper for merging DtpProperties configurations from multiple sources
 *
 * @author yanhom
 * @since 1.0.0
 */
@Slf4j
public final class ConfigMergeHelper {

    private ConfigMergeHelper() {}

    /**
     * Create a deep copy of DtpProperties to preserve existing configuration
     *
     * @param source source DtpProperties
     * @return deep copy of DtpProperties
     */
    public static DtpProperties deepCopy(DtpProperties source) {
        DtpProperties copy = new DtpProperties();
        
        copy.setEnabled(source.isEnabled());
        copy.setEnv(source.getEnv());
        copy.setEnabledBanner(source.isEnabledBanner());
        copy.setEnabledCollect(source.isEnabledCollect());
        copy.setCollectorTypes(source.getCollectorTypes() != null ? new ArrayList<>(source.getCollectorTypes()) : null);
        copy.setLogPath(source.getLogPath());
        copy.setConfigType(source.getConfigType());
        copy.setMonitorInterval(source.getMonitorInterval());
        
        copy.setPlatforms(source.getPlatforms() != null ? new ArrayList<>(source.getPlatforms()) : null);
        copy.setZookeeper(source.getZookeeper());
        copy.setEtcd(source.getEtcd());
        copy.setGlobalExecutorProps(source.getGlobalExecutorProps());
        
        copy.setExecutors(source.getExecutors() != null ? new ArrayList<>(source.getExecutors()) : null);
        copy.setTomcatTp(source.getTomcatTp());
        copy.setJettyTp(source.getJettyTp());
        copy.setUndertowTp(source.getUndertowTp());
        copy.setDubboTp(source.getDubboTp() != null ? new ArrayList<>(source.getDubboTp()) : null);
        copy.setHystrixTp(source.getHystrixTp() != null ? new ArrayList<>(source.getHystrixTp()) : null);
        copy.setRocketMqTp(source.getRocketMqTp() != null ? new ArrayList<>(source.getRocketMqTp()) : null);
        copy.setGrpcTp(source.getGrpcTp() != null ? new ArrayList<>(source.getGrpcTp()) : null);
        copy.setMotanTp(source.getMotanTp() != null ? new ArrayList<>(source.getMotanTp()) : null);
        copy.setOkhttp3Tp(source.getOkhttp3Tp() != null ? new ArrayList<>(source.getOkhttp3Tp()) : null);
        copy.setBrpcTp(source.getBrpcTp() != null ? new ArrayList<>(source.getBrpcTp()) : null);
        copy.setTarsTp(source.getTarsTp() != null ? new ArrayList<>(source.getTarsTp()) : null);
        copy.setSofaTp(source.getSofaTp() != null ? new ArrayList<>(source.getSofaTp()) : null);
        copy.setRabbitmqTp(source.getRabbitmqTp() != null ? new ArrayList<>(source.getRabbitmqTp()) : null);
        copy.setLiteflowTp(source.getLiteflowTp() != null ? new ArrayList<>(source.getLiteflowTp()) : null);
        copy.setThriftTp(source.getThriftTp() != null ? new ArrayList<>(source.getThriftTp()) : null);
        
        return copy;
    }

    /**
     * Merge new configuration into existing configuration, preserving existing values
     * where new configuration doesn't specify them
     *
     * @param existing existing DtpProperties
     * @param newConfig new DtpProperties from configuration refresh
     */
    public static void mergeConfigurations(DtpProperties existing, DtpProperties newConfig) {
        mergeExecutorConfigurations(existing, newConfig);
        mergeAdapterConfigurations(existing, newConfig);
        mergeBasicProperties(existing, newConfig);
    }

    private static void mergeExecutorConfigurations(DtpProperties existing, DtpProperties newConfig) {
        if (CollectionUtils.isEmpty(newConfig.getExecutors())) {
            return;
        }
        
        if (CollectionUtils.isEmpty(existing.getExecutors())) {
            existing.setExecutors(new ArrayList<>(newConfig.getExecutors()));
            return;
        }
        
        for (DtpExecutorProps newExecutor : newConfig.getExecutors()) {
            boolean found = false;
            for (int i = 0; i < existing.getExecutors().size(); i++) {
                DtpExecutorProps existingExecutor = existing.getExecutors().get(i);
                if (Objects.equals(existingExecutor.getThreadPoolName(), newExecutor.getThreadPoolName())) {
                    existing.getExecutors().set(i, newExecutor);
                    found = true;
                    break;
                }
            }
            if (!found) {
                existing.getExecutors().add(newExecutor);
            }
        }
    }

    private static void mergeAdapterConfigurations(DtpProperties existing, DtpProperties newConfig) {
        Field[] fields = DtpProperties.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object newValue = field.get(newConfig);
                if (newValue == null) {
                    continue;
                }
                
                if (field.getGenericType() instanceof ParameterizedType) {
                    ParameterizedType paramType = (ParameterizedType) field.getGenericType();
                    Type[] argTypes = paramType.getActualTypeArguments();
                    if (argTypes.length == 1 && argTypes[0].equals(TpExecutorProps.class)) {
                        @SuppressWarnings("unchecked")
                        List<TpExecutorProps> newList = (List<TpExecutorProps>) newValue;
                        if (CollectionUtils.isNotEmpty(newList)) {
                            field.set(existing, new ArrayList<>(newList));
                        }
                    }
                }
                else if (field.getType().equals(TpExecutorProps.class)) {
                    field.set(existing, newValue);
                }
            } catch (Exception e) {
                log.warn("Failed to merge field: {}", field.getName(), e);
            }
        }
    }

    private static void mergeBasicProperties(DtpProperties existing, DtpProperties newConfig) {
        if (newConfig.getEnv() != null) {
            existing.setEnv(newConfig.getEnv());
        }
        if (newConfig.getLogPath() != null) {
            existing.setLogPath(newConfig.getLogPath());
        }
        if (newConfig.getConfigType() != null) {
            existing.setConfigType(newConfig.getConfigType());
        }
        if (newConfig.getPlatforms() != null) {
            existing.setPlatforms(new ArrayList<>(newConfig.getPlatforms()));
        }
        if (newConfig.getGlobalExecutorProps() != null) {
            existing.setGlobalExecutorProps(newConfig.getGlobalExecutorProps());
        }
        if (newConfig.getCollectorTypes() != null) {
            existing.setCollectorTypes(new ArrayList<>(newConfig.getCollectorTypes()));
        }
        
        existing.setEnabled(newConfig.isEnabled());
        existing.setEnabledBanner(newConfig.isEnabledBanner());
        existing.setEnabledCollect(newConfig.isEnabledCollect());
        existing.setMonitorInterval(newConfig.getMonitorInterval());
    }
}
