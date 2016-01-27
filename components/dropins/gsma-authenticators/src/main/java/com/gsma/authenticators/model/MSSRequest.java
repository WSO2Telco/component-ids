package com.gsma.authenticators.model;

import java.io.Serializable;

/**
 * Created by nilan on 11/24/14.
 */
public class MSSRequest implements Serializable {

    public String msisdnNo;
    public String sendString;

    public String getSendString() {
        return sendString;
    }

    public void setSendString(String sendString) {
        this.sendString = sendString;
    }

    public String getMsisdnNo() {
        return msisdnNo;
    }

    public void setMsisdnNo(String msisdnNo) {
        this.msisdnNo = msisdnNo;
    }



}
