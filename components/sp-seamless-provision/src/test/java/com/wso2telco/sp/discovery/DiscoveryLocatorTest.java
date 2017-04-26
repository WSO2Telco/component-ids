package com.wso2telco.sp.discovery;

import org.junit.*;


import junit.framework.TestCase;

public class DiscoveryLocatorTest extends TestCase {

    /*
     * @BeforeClass
     * public static void initialize() {
     * ignore = 1;
     * }
     */

    @Test
    public void testLocalDiscoveryLocators() {
        // Apis ps = new Apis();
        DiscoveryLocator discoveryLocator = new LocalDiscovery();
        RemoteCredentialDiscovery remoteCredentialDiscovery = new RemoteCredentialDiscovery();
        discoveryLocator.setNextDiscovery(remoteCredentialDiscovery);

        Assert.assertEquals(discoveryLocator.getNextDiscovery(), remoteCredentialDiscovery);

    }
    
    public DiscoveryLocatorTest(String name){
        super(name);
    }
    
    
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(DiscoveryLocatorTest.class);
    }
    /*
     * @After
     * public void collector() {
     * ignore = 0;
     * }
     */

}
