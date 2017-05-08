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
 * Application login model
 */
public class ApplicationLogin implements IDataItem {
    private String application_id;
    private long login_count;
    private Timestamp created_date;

    public String getApplication_id() {
        return application_id;
    }

    public long getLogin_count() {
        return login_count;
    }

    public void setApplication_id(String application_id) {
        this.application_id = application_id;
    }

    public void setLogin_count(long login_count) {
        this.login_count = login_count;
    }

    public Timestamp getCreated_date() {
        return created_date;
    }

    public void setCreated_date(Timestamp created_date) {
        this.created_date = created_date;
    }
}
