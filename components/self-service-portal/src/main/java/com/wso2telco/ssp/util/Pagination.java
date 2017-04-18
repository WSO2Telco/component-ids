/*******************************************************************************
 * Copyright (c) 2015-2017, WSO2.Telco Inc. (http://www.wso2telco.com)
 *
 * All Rights Reserved. WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.wso2telco.ssp.util;

import org.apache.commons.lang.StringUtils;

/**
 * Represents pagination of paged result sets returned from API methods.
 */
public class Pagination {

    private int offset;
    private int limit;
    private int page;

    /**
     * Constructs the pagination by parsing offset and limit values
     * @param page page number
     * @param limit limit per page
     */
    public Pagination(String page, String limit){
        this.page = 1;
        this.limit = 15;

        if(StringUtils.isNotEmpty(page)){
            try{
                this.page = Integer.parseInt(page) > 0 ? Integer.parseInt(page) : 1;
            }catch (Exception e){}
        }

        if(StringUtils.isNotEmpty(limit)){
            try{
                this.limit = Integer.parseInt(limit) > 0 ? Integer.parseInt(limit) : 15;
            }catch (Exception e){}
        }

        this.offset = (this.page - 1) * this.limit;
    }

    /**
     * Gets the offset
     * @return offset
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Gets the limit per page
     * @return limit
     */
    public int getLimit() {
        return limit;
    }

    /**
     * Gets the page number
     * @return page
     */
    public int getPage() {
        return page;
    }
}
