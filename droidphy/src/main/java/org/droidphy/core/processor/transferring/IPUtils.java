package org.droidphy.core.processor.transferring;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class IPUtils {

    public static String getLocalIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return intToIp(ipAddress);
    }

    private static String intToIp(int i) {
        return ( i & 0xFF) + "." +
        ((i >> 8 ) & 0xFF) + "." +
        ((i >> 16 ) & 0xFF) + "." +
        ((i >> 24 ) & 0xFF );
    }
}
