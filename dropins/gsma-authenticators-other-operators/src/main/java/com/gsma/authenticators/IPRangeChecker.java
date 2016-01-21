/*
 * IPRangeChecker.java
 * Jul 15, 2014  11:27:25 AM
 * Roshan.Saputhanthri
 *
 * Copyright (C) Dialog Axiata PLC. All Rights Reserved.
 */

package com.gsma.authenticators;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * <TO-DO> <code>IPRangeChecker</code>
 * @version $Id: IPRangeChecker.java,v 1.00.000
 */

public class IPRangeChecker {

  public static long ipToLong(InetAddress ip) {
		byte[] octets = ip.getAddress();
		long result = 0;
		for (byte octet : octets) {
			result <<= 8;
			result |= octet & 0xff;
		}
		return result;
	}

	public static boolean isValidRange(String ipStart, String ipEnd,
			String ipToCheck) {
		try {
			long ipLo = ipToLong(InetAddress.getByName(ipStart));
			long ipHi = ipToLong(InetAddress.getByName(ipEnd));
			long ipToTest = ipToLong(InetAddress.getByName(ipToCheck));
			return (ipToTest >= ipLo && ipToTest <= ipHi);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static void main(String[] args) {

		System.out.println(isValidRange("122.170.122.0", "122.170.122.255",
				"122.170.122.215"));

	}

}