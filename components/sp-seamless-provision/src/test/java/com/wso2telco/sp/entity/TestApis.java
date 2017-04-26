package com.wso2telco.sp.entity;

import org.junit.*;

import junit.framework.TestCase;

public class TestApis extends TestCase {

    private OperatorId operatorId = null;

    @Before
    public void init() {
        operatorId = new OperatorId();
    }

    @Test
    public void testApisClazz() {
        init();
        Apis apis = new Apis();
        apis.setOperatorid(operatorId);

        Assert.assertEquals(operatorId, apis.getOperatorid());
    }

    private String apisToString(OperatorId operatorId) {
        return "ClassPojo [operatorid = " + operatorId + "]";
    }

    public TestApis(String name) {
        super(name);
    }

    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(TestApis.class);
    }
}
