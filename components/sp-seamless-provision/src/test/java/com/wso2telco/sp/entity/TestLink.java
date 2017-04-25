package com.wso2telco.sp.entity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

public class TestLink extends TestCase {

    private Link link = null;
    private static String HREF = "href";
    private static String REL = "rel";

    @Before
    public void init() {
        link = new Link();
        link.setHref(HREF);
        link.setRel(REL);
    }

    @Test
    public void testLink() {
        init();
        Assert.assertEquals(link.getHref(), HREF);
        Assert.assertEquals(link.getRel(), REL);
        Assert.assertEquals(link.toString(), getLinkToString());
    }

    public TestLink(String name) {
        super(name);
    }

    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(TestLink.class);
    }

    public String getLinkToString() {
        return "ClassPojo [rel = " + REL + ", href = " + HREF + "]";
    }

}
