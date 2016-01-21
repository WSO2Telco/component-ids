package com.gsma.shorten;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * this class use for the get short url base on Provider
 *
 */
public class SelectShortUrl {
    private static Log log = LogFactory.getLog(SelectShortUrl.class);

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
