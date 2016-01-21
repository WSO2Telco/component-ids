package com.gsma.shorten;

/**
 * this interface use for the url short
 *
 */
public interface UrlShorten {

    public String getShortenURL(String longUrl,String accessToken,String shortServiceUrl);

}
