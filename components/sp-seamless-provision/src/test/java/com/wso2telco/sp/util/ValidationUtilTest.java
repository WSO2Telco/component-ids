package com.wso2telco.sp.util;

import org.junit.*;

import com.wso2telco.sp.discovery.exception.DicoveryException;

import junit.framework.TestCase;

public class ValidationUtilTest extends TestCase{

    private static final String CLIENT_ID = "56233566988556222365223";
    private static final String SECTOR = "localhost";
    private static final boolean TRUE = true;
    private static final boolean FALSE = false;
    private static final String EMPTY = "";

    @Test
    public void testValidateInuts() {
        boolean isValid = false;
        try {
            isValid = ValidationUtil.validateInuts(SECTOR, CLIENT_ID);
        } catch (DicoveryException e) {
        }
        Assert.assertEquals(isValid, TRUE);
    }

    @Test
    public void testValidateInutsEmptyClient() {
        boolean isValid = false;
        try {
            isValid = ValidationUtil.validateInuts(SECTOR, EMPTY);
        } catch (DicoveryException e) {
        }
        Assert.assertEquals(isValid, FALSE);
    }

    @Test
    public void testValidateInutsNullClient() {
        boolean isValid = false;
        try {
            isValid = ValidationUtil.validateInuts(SECTOR, null);
        } catch (DicoveryException e) {
        }
        Assert.assertEquals(isValid, FALSE);
    }

    @Test
    public void testValidateInutsEmptySector() {
        boolean isValid = false;
        try {
            isValid = ValidationUtil.validateInuts(EMPTY, CLIENT_ID);
        } catch (DicoveryException e) {
        }
        Assert.assertEquals(isValid, FALSE);
    }

    @Test
    public void testValidateInutsNullSector() {
        boolean isValid = false;
        try {
            isValid = ValidationUtil.validateInuts(null, CLIENT_ID);
        } catch (DicoveryException e) {
        }
        Assert.assertEquals(isValid, FALSE);
    }
    
    public ValidationUtilTest(String name){
        super(name);
    }
    
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(ValidationUtilTest.class);
    }
}
