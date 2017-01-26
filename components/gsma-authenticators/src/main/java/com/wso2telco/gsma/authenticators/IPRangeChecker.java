/*******************************************************************************
 * Copyright (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) 
 * 
 * All Rights Reserved. WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
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

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

 

// TODO: Auto-generated Javadoc
/**
 * The Class IPRangeChecker.
 */
public class IPRangeChecker {
	
    /** The log. */
    private static Log log = LogFactory.getLog(IPRangeChecker.class); 

  /**
   * Ip to long.
   *
   * @param ip the ip
   * @return the long
   */
  public static long ipToLong(InetAddress ip) {
		byte[] octets = ip.getAddress();
		long result = 0;
		for (byte octet : octets) {
			result <<= 8;
			result |= octet & 0xff;
		}
		return result;
	}

	/**
	 * Checks if is valid range.
	 *
	 * @param ipStart the ip start
	 * @param ipEnd the ip end
	 * @param ipToCheck the ip to check
	 * @return true, if is valid range
	 */
	public static boolean isValidRange(String ipStart, String ipEnd,
			String ipToCheck) {
		try {
			long ipLo = ipToLong(InetAddress.getByName(ipStart));
			long ipHi = ipToLong(InetAddress.getByName(ipEnd));
			long ipToTest = ipToLong(InetAddress.getByName(ipToCheck));
			return (ipToTest >= ipLo && ipToTest <= ipHi);
		} catch (UnknownHostException e) {
			log.error("Error ", e);
			return false;
		}
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {

		log.info(isValidRange("122.170.122.0", "122.170.122.255",
				"122.170.122.215"));

	}

}
