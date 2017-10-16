package com.wso2telco.claims;

import com.google.gson.Gson;
import com.wso2telco.dto.Customer;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

public class RemoteClaimsTelco implements RemoteClaims{

    /**
     * The log.
     */
    private static Log log = LogFactory.getLog(RemoteClaimsTelco.class);

    @Override
    public Map<String, Object> getTotalClaims(String operatorEndPoint, String msisdn) {

        Map<String, Object> totalClaims = new HashedMap();
        Customer customer = new Customer();
        try {
            String url = operatorEndPoint + "?msisdn=" + msisdn;
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);
            log.info("Response Code : " + response.getStatusLine().getStatusCode());
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuilder jsonString = new StringBuilder();
            String line = "";

            while ((line = rd.readLine()) != null) {
                jsonString.append(line);
            }
            Gson g = new Gson();
            customer = g.fromJson(jsonString.toString(), Customer.class);

        } catch (Exception ex) {
            log.error(ex);
        }

        totalClaims.put("name", customer.getFirstName());
        totalClaims.put("sub", customer.getSub());
        totalClaims.put("updated_at", customer.getUpdatedAt());
        totalClaims.put("title", customer.getTitle());
        totalClaims.put("given_name", customer.getGivenName());
        totalClaims.put("family_name", customer.getFamilyName());
        totalClaims.put("middle_name", customer.getMiddleName());
        totalClaims.put("gender", customer.getGender());
        totalClaims.put("birthdate", customer.getDob());
        totalClaims.put("AgeVerificationStatus", customer.isAgeVerificationStatus());
        totalClaims.put("street_address", customer.getStreetAddress());
        totalClaims.put("postal_code", customer.getPostalCode());
        totalClaims.put("country", customer.getCountry());
        totalClaims.put("locale", customer.getLocale());
        totalClaims.put("phone_number_alternate", customer.getPhoneNumberAlternate());
        totalClaims.put("email", customer.getEmail());
        totalClaims.put("email_verified", customer.getEmailVerified());
        totalClaims.put("phone_number", customer.getPhoneNumber());
        totalClaims.put("phone_number_country_code", customer.getPhoneNumberCountryCode());
        totalClaims.put("zoneinfo", customer.getZoneinfo());
        totalClaims.put("BillingSegment", customer.getBiilingSegment());
        totalClaims.put("is_lost_stolen", customer.getIsLostStolen());
        totalClaims.put("id_hash", customer.getIdHash());
        totalClaims.put("SubscriptionActivity", customer.getSubscriptionActivity());
        totalClaims.put("LengthOfTenure", customer.getLengthOfTenure());
        totalClaims.put("pairing_change", customer.getPairingChange());
        totalClaims.put("is_roaming", customer.getIsRoaming());
        totalClaims.put("roaming_country", customer.getRoamingCountry());
        totalClaims.put("is_divert_set", customer.getIsDivertSet());
        totalClaims.put("location_country", customer.getLocationCountry());
        totalClaims.put("device_change", customer.getDeviceChange());
        totalClaims.put("msisdn", customer.getMsisdn());

        return totalClaims;
    }

}
