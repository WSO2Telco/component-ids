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

/**
 * Response meta details
 */
public class ResultsMeta {
    private int total_count;
    private int page;
    private int per_page;

    public void setTotal_count(int total_count) {
        this.total_count = total_count;
    }

    public void setPage(int page) {this.page = page;}

    public void setPerPage(int per_page) {
        this.per_page = per_page;
    }

    public int getTotal_count() {
        return total_count;
    }

    public int getPage() {
        return page;
    }

    public int getPerPage() {
        return per_page;
    }
}
