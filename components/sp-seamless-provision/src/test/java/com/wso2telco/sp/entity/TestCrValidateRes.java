package com.wso2telco.sp.entity;

import org.junit.Assert;
import org.junit.Test;

import junit.framework.TestCase;

public class TestCrValidateRes extends TestCase {

    private CrValidateRes crValidateRes = null;
    private Application application;
    private String ORGID = "G8569";
    private String X_CLIENT_ID = "4556481-321364";

    public void init() {
        application = new Application();
        crValidateRes = new CrValidateRes();
    }

    @Test
    public void testCrValidateResClazz() {
        init();
        crValidateRes.setApplication(application);
        crValidateRes.setOrgId(ORGID);
        crValidateRes.setX_client_id(X_CLIENT_ID);

        Assert.assertEquals(crValidateRes.getApplication(), application);
        Assert.assertEquals(crValidateRes.getX_client_id(), X_CLIENT_ID);
        Assert.assertEquals(crValidateRes.getOrgId(), ORGID);
        Assert.assertEquals(getToString(), crValidateRes.toString());
    }

    public TestCrValidateRes(String name) {
        super(name);
    }

    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(TestCrValidateRes.class);
    }

    public String getToString() {
        return "ClassPojo [orgId = " + ORGID + ", application = " + application + ", x_client_id = " + X_CLIENT_ID
                + "]";
    }

}
