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
package com.wso2telco.gsma.authenticators;

import com.gsma.utils.*;

 
// TODO: Auto-generated Javadoc
/**
 * The Class FindOperator.
 */
public class FindOperator {

    /**
     * Gets the operator name.
     *
     * @param msisdn the msisdn
     * @return the operator name
     */
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

    /**
     * Removes the zero from start position.
     *
     * @param msisdn the msisdn
     * @return the string
     */
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
