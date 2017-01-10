/*******************************************************************************
 * Copyright (c) 2015-2017, WSO2.Telco Inc. (http://www.wso2telco.com)
 *
 * All Rights Reserved. WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.wso2telco.ids.datapublisher;

import com.wso2telco.ids.datapublisher.util.FileUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;

/**
 * Global publisher to publish ids data
 */
public class IdsAgent {

    private static Log log = LogFactory.getLog(IdsAgent.class);
    private DataPublisher publisher = null;
    private boolean isPublisherEnabled = true;

    private IdsAgent(){
        initPublisher();
    }

    private static class AgentFactoryHolder {
        private static final IdsAgent INSTANCE = new IdsAgent();
    }

    public static IdsAgent getInstance(){
        return AgentFactoryHolder.INSTANCE;
    }

    private void initPublisher(){
        setPublisherEnabled(Boolean.parseBoolean(FileUtil.getApplicationProperty("publisher_enable")));
        if(isPublisherEnabled()) {
            AgentHolder.setConfigPath(CarbonUtils.getCarbonConfigDirPath() + File.separator + "data-bridge" +
                    File.separator + "data-agent-conf.xml");
            System.setProperty("javax.net.ssl.trustStore", System.getProperty("carbon.home") +
                    File.separator + "repository/resources/security/client-truststore.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
            try {
                publisher = new DataPublisher("Thrift", FileUtil.getApplicationProperty("das_url"),
                        FileUtil.getApplicationProperty("das_secure_url"),
                        FileUtil.getApplicationProperty("username"),
                        FileUtil.getApplicationProperty("password"));
            } catch (DataEndpointAgentConfigurationException e) {
                e.printStackTrace();
            } catch (DataEndpointException e) {
                e.printStackTrace();
            } catch (DataEndpointConfigurationException e) {
                e.printStackTrace();
            } catch (DataEndpointAuthenticationException e) {
                e.printStackTrace();
            } catch (TransportException e) {
                e.printStackTrace();
            }
        }else{
            log.warn("IDS data agent disabled");
        }
    }

    public  void publish(String streamName, String streamVersion, long timestamp, Object[] dataArray){
        if(isPublisherEnabled()) {
            String streamId = DataBridgeCommonsUtils.generateStreamId(streamName, streamVersion);
            publisher.publish(streamId, timestamp, null, null, dataArray);
        }else {
            log.warn("IDS data agent disabled");
        }
    }

    public boolean isPublisherEnabled() {
        return isPublisherEnabled;
    }

    public void setPublisherEnabled(boolean publisherEnabled) {
        isPublisherEnabled = publisherEnabled;
    }
}
