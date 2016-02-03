/*******************************************************************************
 * Copyright (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) 
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
package com.wso2telco.gsma.authenticators;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


// TODO: Auto-generated Javadoc
/**
 * The Class SessionExpire.
 */
public class SessionExpire implements Runnable {

    /** The log. */
    private static Log log = LogFactory.getLog(SessionExpire.class);
    
    /** The session id. */
    private String sessionId;
    
    /** The t. */
    private Thread t;
    
    /** The exp time. */
    private long expTime=120000;

    /**
     * Instantiates a new session expire.
     *
     * @param sessionId the session id
     */
    public SessionExpire(String sessionId){
        this.sessionId=sessionId;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        try {
            Thread.sleep(expTime);
            try {
                DBUtils.updateUserResponse(sessionId,"EXPIRED");

            } catch (Exception e) {
                log.error("Error while connecting to DB", e);
            }
        } catch (InterruptedException e) {
            log.error("Error run on SessionExpire", e);
        }
    }

    /**
     * Start.
     */
    public void start ()
    {
        if (t == null)
        {
            t = new Thread (this, sessionId);
            t.start ();
        }
    }


}
