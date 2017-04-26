package com.wso2telco.sp.entity;

import org.junit.*;

import junit.framework.TestCase;

public class TestResponse extends TestCase {

    private Response response = null;

    private String SERVING_OPERATOR = "SERVING_OPERATOR";

    private String CLIENT_SECRET = "CLIENT_SECRET";

    private Apis apis = null;

    private String CLIENT_ID = "CLIENT_ID";

    private String CURRENCY = "CURRENCY";

    private String COUNTRY = "COUNTRY";

    @Before
    public void init() {
        response = new Response();
        apis = new Apis();

        response.setApis(apis);
        response.setClient_id(CLIENT_ID);
        response.setClient_secret(CLIENT_SECRET);
        response.setCountry(COUNTRY);
        response.setCurrency(CURRENCY);
        response.setServing_operator(SERVING_OPERATOR);
    }

    public void testResponseClazz() {
        init();
        Assert.assertEquals(response.getClient_id(), CLIENT_ID);
        Assert.assertEquals(response.getClient_secret(), CLIENT_SECRET);
        Assert.assertEquals(response.getCountry(), COUNTRY);
        Assert.assertEquals(response.getCurrency(), CURRENCY);
        Assert.assertEquals(response.getApis(), apis);
        Assert.assertEquals(response.getServing_operator(), SERVING_OPERATOR);
        Assert.assertEquals(response.toString(), getResponseToString());
    }

    public TestResponse(String name) {
        super(name);
    }

    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(TestResponse.class);
    }

    public String getResponseToString() {
        return "ClassPojo [serving_operator = " + SERVING_OPERATOR + ", client_secret = " + CLIENT_SECRET + ", apis = "
                + apis + ", client_id = " + CLIENT_ID + ", currency = " + CURRENCY + ", country = " + COUNTRY + "]";

    }

}
