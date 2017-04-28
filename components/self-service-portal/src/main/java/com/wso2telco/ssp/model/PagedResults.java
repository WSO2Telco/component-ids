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

import java.util.List;

public class PagedResults {

    private List<IDataItem> items;
    private ResultsMeta meta;

    public void setMeta(ResultsMeta meta) { this.meta = meta; }

    public ResultsMeta getMeta() { return meta; }

    public void setItems(List<IDataItem> items) { this.items = items; }

    public List<IDataItem> getItems() { return items; }
}
