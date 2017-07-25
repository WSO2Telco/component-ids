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
package com.wso2telco.sp.entity;

public class Response {
    private String serving_operator;

    private String client_secret;

    private Apis apis;

    private String client_id;

    private String currency;

    private String country;

    public String getServing_operator() {
        return serving_operator;
    }

    public void setServing_operator(String serving_operator) {
        this.serving_operator = serving_operator;
    }

    public String getClient_secret() {
        return client_secret;
    }

    public void setClient_secret(String client_secret) {
        this.client_secret = client_secret;
    }

    public Apis getApis() {
        return apis;
    }

    public void setApis(Apis apis) {
        this.apis = apis;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String toString() {
        return "ClassPojo [serving_operator = " + serving_operator + ", client_secret = " + client_secret + ", apis = "
                + apis + ", client_id = " + client_id + ", currency = " + currency + ", country = " + country + "]";
    }
}
