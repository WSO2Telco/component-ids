package com.wso2telco.sp.entity;

import org.junit.*;

import junit.framework.TestCase;

public class TestOperatorId extends TestCase {

    private OperatorId operatorId = null;
    private Link[] links = null;

    public void init() {
        operatorId = new OperatorId();
        links = new Link[1];
        Link link_1 = new Link();
        links[0] = link_1;
    }

    @Test
    public void testOperatorIdClazz() {
        init();
        operatorId.setLink(links);
        Assert.assertEquals(operatorId.getLink(), links);
        Assert.assertEquals(operatorId.toString(), getToString());

    }

    public TestOperatorId(String name) {
        super(name);
    }

    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(TestOperatorId.class);
    }

    public String getToString() {
        return "ClassPojo [link = " + links + "]";
    }

}
