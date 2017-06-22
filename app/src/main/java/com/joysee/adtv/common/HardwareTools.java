package com.joysee.adtv.common;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;

public class HardwareTools {
	// get mac address
	public static String getMac(Context context) {
		if (context == null) {
			return null;
		}
		String mac;
		if (SystemProperties.get("ubootenv.var.ethaddr", "") != null) {
			mac = SystemProperties.get("ubootenv.var.ethaddr", "");
		} else {
			WifiManager wifi = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			WifiInfo info = wifi.getConnectionInfo();
			mac = info.getMacAddress();
			System.out.println("mac/物理地址:" + info.getMacAddress());
		}
		return mac;
	}
}
