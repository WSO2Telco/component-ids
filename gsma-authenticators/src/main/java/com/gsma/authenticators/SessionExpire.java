package com.gsma.authenticators;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class SessionExpire implements Runnable {

    private static Log log = LogFactory.getLog(SessionExpire.class);
    private String sessionId;
    private Thread t;
    private long expTime=120000;

    public SessionExpire(String sessionId){
        this.sessionId=sessionId;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(expTime);
            try {
                DBUtils.updateUserResponse(sessionId,"EXPIRED");

            } catch (Exception e) {
                log.error("Error while connecting to DB", e);
            }
        } catch (java.lang.InterruptedException e) {
            log.error("Error run on SessionExpire", e);
        }
    }

    public void start ()
    {
        if (t == null)
        {
            t = new Thread (this, sessionId);
            t.start ();
        }
    }


}
