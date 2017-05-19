/*******************************************************************************
 * Copyright  (c) 2015-2017, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
 *
 * WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.wso2telco.ssp.model;

import java.sql.Timestamp;

/**
 * Login history model
 */
public class LoginHistory implements IDataItem{
    private int id;
    private String reqtype;
    private String application_id;
    private String authenticated_user;
    private boolean isauthenticated;
    private String authenticators;
    private String ipaddress;
    private String created;
    private Timestamp created_date;
    private String lastupdated;
    private Timestamp lastupdated_date;
    private long duration;

    public long getDuration() { return duration; }

    public void setDuration(long duration) { this.duration = duration; }

    public void setId(int id) {
        this.id = id;
    }

    public void setReqtype(String reqtype) { this.reqtype = reqtype; }

    public void setApplication_id(String application_id) {
        this.application_id = application_id;
    }

    public void setAuthenticated_user(String authenticated_user) {
        this.authenticated_user = authenticated_user;
    }

    public void setIsauthenticated(boolean isauthenticated) {
        this.isauthenticated = isauthenticated;
    }

    public void setAuthenticators(String authenticators) {
        this.authenticators = authenticators;
    }

    public void setIpaddress(String ipaddress) {
        this.ipaddress = ipaddress;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public void setCreated_date(Timestamp created_date) {
        this.created_date = created_date;
    }

    public void setLastupdated(String lastupdated) {
        this.lastupdated = lastupdated;
    }

    public void setLastupdated_date(Timestamp lastupdated_date) {
        this.lastupdated_date = lastupdated_date;
    }

    public int getId() {
        return id;
    }

    public String getReqtype() {
        return reqtype;
    }

    public String getApplication_id() {
        return application_id;
    }

    public String getAuthenticated_user() {
        return authenticated_user;
    }

    public boolean isauthenticated() {
        return isauthenticated;
    }

    public String getAuthenticators() {
        return authenticators;
    }

    public String getIpaddress() {
        return ipaddress;
    }

    public String getCreated() {
        return created;
    }

    public Timestamp getCreated_date() {
        return created_date;
    }

    public String getLastupdated() {
        return lastupdated;
    }

    public Timestamp getLastupdated_date() {
        return lastupdated_date;
    }


}
