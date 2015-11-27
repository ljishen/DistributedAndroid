package org.droidphy.core.network.helpers;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SystemService;

import java.net.InetAddress;
import java.net.UnknownHostException;

@EBean(scope = EBean.Scope.Singleton)
public class NetworkHelper {
    private static final long HOST_ADDRESS_CACHE_TIMEOUT_IN_MILLIS = 60 * 1000;

    @SystemService
    WifiManager wifiManager;

    private String hostAddress;
    private long lastAccessTimestamp;

    private MulticastLock multiCastLock;

    public InetAddress getLocalHost() {
        // Require permission android.permission.ACCESS_WIFI_STATE
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null) {
            return null;
        }

        int ipAddress = wifiInfo.getIpAddress();

        byte[] addressBytes = { (byte)(0xff & ipAddress),
                (byte)(0xff & (ipAddress >> 8)),
                (byte)(0xff & (ipAddress >> 16)),
                (byte)(0xff & (ipAddress >> 24)) };
        try {
            return InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException e) {
            throw new AssertionError();
        }
    }

    public String getLocalHostAddress() {
        long currentTimeMillis = System.currentTimeMillis();
        if (hostAddress == null ||
                currentTimeMillis - lastAccessTimestamp > HOST_ADDRESS_CACHE_TIMEOUT_IN_MILLIS) {
            hostAddress = getLocalHost().getHostAddress();
        }
        lastAccessTimestamp = currentTimeMillis;

        return hostAddress;
    }

    public MulticastLock getOrCreateMulticastLock(String tag) {
        if (multiCastLock == null) {
            multiCastLock = wifiManager.createMulticastLock(tag);
        }
        return multiCastLock;
    }
}
