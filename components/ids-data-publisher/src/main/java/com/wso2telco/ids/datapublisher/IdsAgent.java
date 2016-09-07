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
