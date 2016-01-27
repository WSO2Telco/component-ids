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
package com.wso2telco.gsma.shorten;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

 
// TODO: Auto-generated Javadoc
/**
 * The Class SelectShortUrl.
 */
public class SelectShortUrl {
    
    /** The log. */
    private static Log log = LogFactory.getLog(SelectShortUrl.class);

    /**
     * Gets the short url.
     *
     * @param providerName the provider name
     * @param longUrl the long url
     * @param key the key
     * @param shortServiceUrl the short service url
     * @return the short url
     */
    public String getShortUrl(String providerName,String longUrl,String key,String shortServiceUrl){
        String shortUrl=null;

        try {

        Class clazz = Class.forName(providerName);
        UrlShorten urlShorten= (UrlShorten)clazz.newInstance();
        shortUrl=urlShorten.getShortenURL(longUrl,key,shortServiceUrl);

        } catch (InstantiationException e) {
            log.info("Instantiation Exception when object create "+e);
        } catch (IllegalAccessException e) {
            log.info("Instantiation Exception when object create "+e);
            e.printStackTrace();
        } catch (ClassNotFoundException e){
            log.info("Instantiation Exception when object create "+e);
            e.printStackTrace();
        }

        return shortUrl;
    }
}
