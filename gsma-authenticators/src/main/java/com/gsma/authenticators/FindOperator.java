package com.gsma.authenticators;

import com.gsma.utils.*;

/**
 * Created by paraparan on 5/15/15.
 */
public class FindOperator {

    public String getOperatorName(String msisdn){
        ReadMobileCountryConfig readMobileCountryConfig = new ReadMobileCountryConfig();
        //Check msisdn value notequal null and grater than 11
        if( msisdn != "null" && msisdn.length() > 5){
            //  remove tel:+ value form msisdn if any
            if(msisdn.startsWith("tel:+")){
                msisdn = msisdn.substring(5, msisdn.length());
            }

            msisdn = removeZeroFromStartPosition(msisdn);
            String operatorfirsttwodigit = msisdn.substring(0,2);
            String operatorthreetwodigit = msisdn.substring(0,3);
            String operatorfirstfourdigit = msisdn.substring(0,4);
            if(operatorfirsttwodigit.equalsIgnoreCase("94")){
                return readMobileCountryConfig.findOperatorName(operatorfirstfourdigit);
            } else if(operatorfirsttwodigit.equalsIgnoreCase("60")){
                return readMobileCountryConfig.findOperatorName(operatorfirsttwodigit);
            }

            if(operatorthreetwodigit.equalsIgnoreCase("880")){
                return readMobileCountryConfig.findOperatorName(operatorthreetwodigit);
            }
        }
        return "";
    }

    //removing zero in front of msisdn
    public String removeZeroFromStartPosition(String msisdn){
        for (int i = 0; i < msisdn.length(); i++) {
            if(msisdn.substring(0,1).toString().equals("0")){
                msisdn = msisdn.substring(1,msisdn.length());
            }else {
                return msisdn;
            }
        }
        return msisdn;
    }
}
