package com.gsma.authenticators;

import com.google.gson.Gson;
import com.gsma.authenticators.config.MSSServiceURL;
import com.gsma.authenticators.model.MSSRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Created by nilan on 12/8/14.
 */
public class MSSRestClient extends Thread{

    String contextIdentifier;
    MSSRequest mssRequest;

    public MSSRestClient(String contextIdentifier, MSSRequest mssRequest)
    {
        this.contextIdentifier =contextIdentifier;
        this.mssRequest=mssRequest;

    }

    @Override
    public void run()
    {
        try {

            Gson gson = new Gson();
            org.apache.http.client.HttpClient client = new DefaultHttpClient();

            String serviceURL = String.format(MSSServiceURL.MSS_SIGNATURE_SERVICE, DataHolder.getInstance().getMobileConnectConfig().getMSS().getEndpoint());
            String json = gson.toJson(mssRequest);

            HttpPost httprequest = new HttpPost(serviceURL);
            httprequest.addHeader("Accept", "application/json");
            StringEntity entity = new StringEntity(json, "application/json", "ISO-8859-1");
            httprequest.setEntity(entity);
            HttpResponse httpResponse=client.execute(httprequest);
            if(httpResponse.getStatusLine().getStatusCode()== DataHolder.getInstance().getMobileConnectConfig().getMSS().getSuccessStatus())
            {
                DBUtils.updateUserResponse(contextIdentifier, String.valueOf(UserResponse.APPROVED));

            }
            else{
                DBUtils.updateUserResponse(contextIdentifier, String.valueOf(UserResponse.REJECTED));

            }

        }catch(Exception ex){
            ex.printStackTrace();

        }

    }


    private enum UserResponse {
        PENDING,
        APPROVED,
        REJECTED
    }


}
