/*******************************************************************************
 * Copyright  (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
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
package com.wso2telco.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

// TODO: Auto-generated Javadoc

/**
 * The Class Scope.
 */
@XmlRootElement(name = "Scope")
@XmlAccessorType(XmlAccessType.FIELD)
public class Scope {

    /**
     * The name.
     */
    @XmlElement(name = "Name")
    private String name;

    /**
     * The claims.
     */
    @XmlElement(name = "Claims")
    private Claims claims;

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the claims.
     *
     * @return the claims
     */
    public Claims getClaims() {
        return claims;
    }

    /**
     * Sets the claims.
     *
     * @param claims the new claims
     */
    public void setClaims(Claims claims) {
        this.claims = claims;
    }
}
