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

package org.dromara.dynamictp.common.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * DefaultValueUtil related
 *
 * @author yanhom
 * @since 1.2.1
 **/
public final class DefaultValueUtil {

    private DefaultValueUtil() {
    }

    public static <T extends Number> void setIfZero(Supplier<T> getter, Consumer<T> setter, T defaultValue) {
        T value = getter.get();
        if (value == null || value.intValue() == 0) {
            setter.accept(defaultValue);
        }
    }
}
