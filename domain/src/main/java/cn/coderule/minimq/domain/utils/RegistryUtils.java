/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */package cn.coderule.minimq.domain.utils;

import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class RegistryUtils {
    public static final String INSTANCE_PREFIX = "MQ_INST_";
    public static final String INSTANCE_REGEX = INSTANCE_PREFIX + "\\w+_\\w+";
    public static final String ENDPOINT_PREFIX = "(\\w+://|)";
    public static final Pattern NAMESRV_ENDPOINT_PATTERN = Pattern.compile("^http://.*");
    public static final Pattern INST_ENDPOINT_PATTERN = Pattern.compile("^" + ENDPOINT_PREFIX + INSTANCE_REGEX + "\\..*");
    public static final String NAMESRV_ADDR_ENV = "NAMESRV_ADDR";
    public static final String NAMESRV_ADDR_PROPERTY = "rocketmq.namesrv.addr";

    public static String getRegistryAddresses() {
        return System.getProperty(NAMESRV_ADDR_PROPERTY, System.getenv(NAMESRV_ADDR_ENV));
    }

    public static boolean validateEndpoint(String endpoint) {
        return INST_ENDPOINT_PATTERN.matcher(endpoint).matches();
    }

    public static String parseEndpoint(String endpoint) {
        if (StringUtils.isEmpty(endpoint)) {
            return null;
        }
        return endpoint.substring(endpoint.lastIndexOf("/") + 1, endpoint.indexOf('.'));
    }

    public static String geEndpoint(String nameSrvEndpoint) {
        if (StringUtils.isEmpty(nameSrvEndpoint)) {
            return null;
        }
        return nameSrvEndpoint.substring(nameSrvEndpoint.lastIndexOf('/') + 1);
    }
}
