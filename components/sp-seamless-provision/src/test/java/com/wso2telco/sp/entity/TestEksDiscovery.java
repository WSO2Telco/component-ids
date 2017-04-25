package com.wso2telco.sp.entity;

import org.junit.*;

import junit.framework.TestCase;


public class TestEksDiscovery extends TestCase {

    private EksDiscovery eksDiscovery;
    private String TTL="6322563";
    private Response response; 
    
    public void init(){
        eksDiscovery = new EksDiscovery();
        response = new Response();
        eksDiscovery.setResponse(response);
        eksDiscovery.setTtl(TTL);
    }
    
    @Test
    public void testEksDiscoveryClazz(){
        init();
        Assert.assertEquals(eksDiscovery.getTtl(), TTL);
        Assert.assertEquals(eksDiscovery.getResponse(), response);
        Assert.assertEquals(eksDiscovery.toString(), getEksDiscoveryToString());
    }
    
    public String getEksDiscoveryToString() {
        return "ClassPojo [response = " + response + ", ttl = " + TTL + "]";
    }
    
    public TestEksDiscovery(String name){
        super(name);
    }
    
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(TestEksDiscovery.class);
    }
}
