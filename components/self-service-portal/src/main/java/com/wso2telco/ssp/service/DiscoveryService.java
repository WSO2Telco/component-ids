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
package com.wso2telco.ssp.service;

import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.ssp.api.Endpoints;
import com.wso2telco.ssp.exception.ApiException;
import com.wso2telco.ssp.util.Constants;
import com.wso2telco.ssp.util.HttpClientProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;

/**
 * Discovery service to identify the operator from the msisdn
 */
public class DiscoveryService {

    private static Log log = LogFactory.getLog(Endpoints.class);

    private static MobileConnectConfig.SelfServicePortalConfig selfServicePortalConfig;

    private static Map<String, String> operatorDiscoveryNameMap;


    static {
        selfServicePortalConfig =
                new ConfigurationServiceImpl().getDataHolder().getMobileConnectConfig().getSelfServicePortalConfig();
        operatorDiscoveryNameMap =
                new ConfigurationServiceImpl().getDataHolder().getMobileConnectConfig().getOperatorDiscoveryNameMap();
    }

    /**
     * Get operator name from the msisdn
     * @param msisdn msisdn to get the operator
     * @return operator name
     * @throws ApiException
     */
    public static String getOperator(String msisdn) throws ApiException {
        HttpClient client = HttpClientProvider.GetHttpClient();
        HttpPost httpPost = new HttpPost(selfServicePortalConfig.getDiscoveryAPICall());

        httpPost.setHeader(Constants.HEADER_AUTHORIZATION, "Basic " + selfServicePortalConfig.getDiscoveryAPIToken());
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

        ArrayList<BasicNameValuePair> postParameters = new ArrayList<>();
        postParameters.add(new BasicNameValuePair("MSISDN", msisdn));

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(postParameters));

            HttpResponse httpResponse = client.execute(httpPost);

            JSONObject jsonResponse = getJsonObjectFromHttpResponse(httpResponse);

            if(!jsonResponse.isNull("response") && !jsonResponse.getJSONObject("response").isNull("serving_operator")){
                String operatorName = jsonResponse.getJSONObject("response").getString("serving_operator");
                if(operatorDiscoveryNameMap.containsKey(operatorName)){
                    return operatorDiscoveryNameMap.get(operatorName);
                }else{
                    return operatorName;
                }
            }
        }catch (IOException e){
            throw new ApiException("Server Error", "server_error", Response.Status.INTERNAL_SERVER_ERROR);
        }

        return "spark";
    }

    private static JSONObject getJsonObjectFromHttpResponse(HttpResponse httpResponse) throws IOException {
        InputStream ips  = httpResponse.getEntity().getContent();
        BufferedReader buf = new BufferedReader(new InputStreamReader(ips,"UTF-8"));

        StringBuilder sb = new StringBuilder();
        String s;
        while(true )
        {
            s = buf.readLine();
            if(s==null || s.length()==0)
                break;
            sb.append(s);

        }
        buf.close();
        ips.close();

        return new JSONObject(sb.toString());
    }
}
