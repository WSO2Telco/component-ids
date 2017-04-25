package com.wso2telco.sp.discovery;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.Map;

public interface InlineConnectionStrategies {

    public void setConnectionRequestMethod(String requestMethod, HttpURLConnection conn) throws ProtocolException;
    public void setRequestProperties(Map<String, String> requestPrperties, HttpURLConnection conn);
    public boolean setOutPutStrategy(String data, HttpURLConnection conn);
    public void writeToOutputStream(String data, HttpURLConnection conn) throws IOException;
    public String getJsonBy(InputStream inputStream) throws IOException;
}
