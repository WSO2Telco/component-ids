package com.wso2telco.sp.entity;

import org.junit.*;

import junit.framework.TestCase;

public class TestSupportedApis extends TestCase {

    private static final String NAME = "NAME";
    private SupportedApis supportedApis = null;

    @Before
    public void init() {
        supportedApis = new SupportedApis();
        supportedApis.setName(NAME);
    }

    @Test
    public void testSupportedApisClazz() {
        init();
        Assert.assertEquals(supportedApis.getName(), NAME);
        Assert.assertEquals(supportedApis.toString(), getSupportedApisToString());
    }

    private String getSupportedApisToString() {
        return "ClassPojo [name = " + NAME + "]";
    }

    public TestSupportedApis(String name) {
        super(name);
    }

    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(TestSupportedApis.class);
    }

}
