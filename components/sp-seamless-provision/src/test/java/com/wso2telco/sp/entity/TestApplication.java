package com.wso2telco.sp.entity;

import org.junit.*;

import junit.framework.TestCase;

public class TestApplication extends TestCase {

    private static final String APPNAME = "APPNAME";
    private static final String APPCREDID = "APPCREDID";
    private static final String APPSTATUS = "APPSTATUS";
    private static final String APPTYPE = "APPTYPE";
    private static final String DESCRIPTION = "DESCRIPTION";
    private static final String DEVID = "DEVID";
    private static final String DEVNAME = "DEVNAME";
    private static final String DEVORGID = "DEVORGID";
    private static final String DEVSTATUS = "DEVSTATUS";
    private static final String REDIRECTURI = "REDIRECTURI";
    private static SupportedApis[] supportedApis = null;
    private static SupportedApis supportedApis_1 = null;
    private static SupportedApis supportedApis_2 = null;

    private Application application;

    
    public void init() {

        supportedApis = new SupportedApis[2];
        supportedApis_1 = new SupportedApis();
        supportedApis_2 = new SupportedApis();
        supportedApis[0] = supportedApis_1;
        supportedApis[1] = supportedApis_2;

        application = new Application();
        application.setAppName(APPNAME);
        application.setAppCredId(APPCREDID);
        application.setAppStatus(APPSTATUS);
        application.setAppType(APPTYPE);
        application.setDescription(DESCRIPTION);
        application.setDevId(DEVID);
        application.setDevName(DEVNAME);
        application.setDevOrgId(DEVORGID);
        application.setDevStatus(DEVSTATUS);
        application.setRedirectUri(REDIRECTURI);
        application.setSupportedApis(supportedApis);

    }

    
    public void testApplicationClazz() {
        init();
        Assert.assertEquals(application.getAppCredId(), APPCREDID);
        Assert.assertEquals(application.getAppName(), APPNAME);
        Assert.assertEquals(application.getAppStatus(), APPSTATUS);
        Assert.assertEquals(application.getAppType(), APPTYPE);
        Assert.assertEquals(application.getDescription(), DESCRIPTION);
        Assert.assertEquals(application.getDevId(), DEVID);
        Assert.assertEquals(application.getDevName(), DEVNAME);
        Assert.assertEquals(application.getDevOrgId(), DEVORGID);
        Assert.assertEquals(application.getDevStatus(), DEVSTATUS);
        Assert.assertEquals(application.getRedirectUri(), REDIRECTURI);
        Assert.assertEquals(application.getSupportedApis(), supportedApis);

    }

    public TestApplication(String name) {
        super(name);
    }

    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(TestApplication.class);
    }

    private String getApplicationToStting() {
        return "ClassPojo [appName = " + APPNAME + ", appStatus = " + APPSTATUS + ", description = " + DESCRIPTION
                + ", devStatus = " + DEVSTATUS + ", devName = " + DEVNAME + ", supportedApis = " + supportedApis
                + ", devId = " + DEVID + ", devOrgId = " + DEVORGID + ", redirectUri = " + REDIRECTURI + ", appType = "
                + APPTYPE + ", appCredId = " + APPCREDID + "]";
    }

}
