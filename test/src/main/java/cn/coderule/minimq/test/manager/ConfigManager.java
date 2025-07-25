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
package cn.coderule.minimq.test.manager;

import cn.coderule.common.util.lang.string.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.rocketmq.client.apis.ClientConfiguration;

public class ConfigManager {
    public static final String BASE_CONFIG = "api-test.json";
    public static final String DEFAULT_ACCOUNT = "default";
    public static final String DEFAULT_CLUSTER = "defaultCluster";
    public static final int DEFAULT_QUEUE_NUM = 8;

    private static JSONObject config = null;

    public static void init() throws Exception {
        if (null != config) {
            return;
        }

        config = loadConfig(BASE_CONFIG);
    }

    public static JSONObject getConfig() {
        return config;
    }

    public static ClientConfiguration buildClientConfig() {
        return buildClientConfig(DEFAULT_ACCOUNT);
    }

    public static ClientConfiguration buildClientConfig(String accountName) {
        if (accountName == null) {
            accountName = DEFAULT_ACCOUNT;
        }

        JSONObject account = config.getJSONObject("accounts").getJSONObject(accountName);
        if (account == null) {
            throw new IllegalArgumentException("can't found account info: " + accountName);
        }

        //SessionCredentialsProvider sessionCredentialsProvider =
        //    new StaticSessionCredentialsProvider(account.getString("accessKey"), account.getString("secretKey"));

        return ClientConfiguration.newBuilder()
            .setEndpoints(config.getString("proxyAddr"))
            //.enableSsl(false)
            //.setCredentialProvider(sessionCredentialsProvider)
            .build();
    }

    public static JSONObject loadConfig(String fileName) throws Exception {
        if (StringUtil.isBlank(fileName)) {
            throw new IllegalArgumentException("fileName can't be blank");
        }

        try (InputStream inputStream = ConfigManager.class.getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream == null) {
                throw new IOException("config file not exists: " + fileName);
            }

            String fileData = CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8));
            return JSON.parseObject(fileData);
        }
    }

}
