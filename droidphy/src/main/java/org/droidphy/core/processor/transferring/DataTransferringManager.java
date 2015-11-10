package org.droidphy.core.processor.transferring;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;


import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import org.droidphy.core.processor.JmDnsServerThreadProcessor;
import org.droidphy.core.controller.MyApplication;
import org.droidphy.core.processor.ClientProcessor;


public class DataTransferringManager {


    public static int SERVICE_INFO_PORT = 8856;
    private String SERVICE_INFO_TYPE = "_sample_jmdns._tcp.local.";
    private String SERVICE_INFO_NAME = "sample_jmdns_service";
    private String SERVICE_INFO_PROPERTY_IP_VERSION = "ipv4";
    private String SERVICE_INFO_PROPERTY_DEVICE = "device";


    private JmDNS jmdns = null;
    private ServiceListener listener = null;
    private ServiceInfo serviceInfo;
    private MulticastLock multiCastLock;
    private boolean registered;
    private JmDnsServerThreadProcessor serverThreadProcessor = new JmDnsServerThreadProcessor();


    public void startDataTransferring(final Context context) {

        WifiManager wifi = (WifiManager) context.getSystemService(android.content.Context.WIFI_SERVICE);
        changeMultiCastLock(wifi);

        try {
            if (jmdns == null) {

                InetAddress addr = getInetAddress(wifi);
                jmdns = JmDNS.create(addr);
                jmdns.addServiceListener(SERVICE_INFO_TYPE, listener = new ServiceListener() {
                    public void serviceResolved(ServiceEvent ev) {
                    }

                    public void serviceRemoved(ServiceEvent ev) {
                    }

                    public void serviceAdded(ServiceEvent event) {
                        jmdns.requestServiceInfo(event.getType(), event.getName(), 1);
                    }
                });
                Hashtable<String, String> settings = setSettingsHashTable(context);
                serviceInfo = ServiceInfo.create(SERVICE_INFO_TYPE, SERVICE_INFO_NAME, SERVICE_INFO_PORT, 0, 0, true, settings);
                jmdns.registerService(serviceInfo);
                serverThreadProcessor.startServerProcessorThread(context);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerService() {
        if (jmdns != null)
            try {
                jmdns.registerService(serviceInfo);
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public void unregisterService() {
        if (jmdns != null)
            jmdns.unregisterService(serviceInfo);
    }

    public void stopDataTransferring() {
        if (jmdns != null) {
            if (listener != null) {
                jmdns.removeServiceListener(SERVICE_INFO_TYPE, listener);
                listener = null;
            }
            jmdns.unregisterAllServices();
            registered = false;
            try {
                jmdns.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            jmdns = null;
        }
        serverThreadProcessor.stopServerProcessorThread();
        if (multiCastLock != null && multiCastLock.isHeld())
            multiCastLock.release();
    }


    private InetAddress getInetAddress(WifiManager wifiManager) throws IOException {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int addrIntIp = wifiInfo.getIpAddress();

        byte[] byteaddr = new byte[]{
                (byte) (addrIntIp & 0xff),
                (byte) (addrIntIp >> 8 & 0xff),
                (byte) (addrIntIp >> 16 & 0xff),
                (byte) (addrIntIp >> 24 & 0xff)
        };
        return InetAddress.getByAddress(byteaddr);
    }

    private void changeMultiCastLock(WifiManager wifiManager) {
        if (multiCastLock != null && multiCastLock.isHeld())
            multiCastLock.release();

        if (multiCastLock == null) {
            multiCastLock = wifiManager.createMulticastLock("mylockthereturn");
            multiCastLock.setReferenceCounted(true);
        }

        multiCastLock.acquire();
    }

    private Hashtable<String, String> setSettingsHashTable(Context context) {
        Hashtable<String, String> settings = new Hashtable<>();
        settings.put(SERVICE_INFO_PROPERTY_DEVICE, ((MyApplication) context.getApplicationContext()).getDeviceId());
        settings.put(SERVICE_INFO_PROPERTY_IP_VERSION, IPUtils.getLocalIpAddress(context));
        return settings;
    }

    public JmDNS getJmDNS() {
        return jmdns;
    }

    private String getIPv4FromServiceInfo(ServiceInfo serviceInfo) {
        return serviceInfo.getPropertyString(SERVICE_INFO_PROPERTY_IP_VERSION);
    }


    public void sendMessageToAllDevicesInNetwork(final Context context, String message) {
        if (jmdns != null) {

            Set<String> ipAddressesSet = getNeighborDevicesIpAddressesSet(context);

            for (java.util.Iterator iterator = ipAddressesSet.iterator(); iterator.hasNext(); ) {
                String serverIpAddress = (String) iterator.next();
                ClientProcessor clientProcessor = new ClientProcessor(serverIpAddress, context);
                clientProcessor.sendSimpleMessageToOtherDevice(message);
            }
        }
    }


    private Set<String> getNeighborDevicesIpAddressesSet(Context context) {

        Set<String> ipAddressesSet = new HashSet<>();
        ServiceInfo[] serviceInfoList = jmdns.list(SERVICE_INFO_TYPE);

        for (int index = 0; index < serviceInfoList.length; index++) {
            ServiceInfo currentServiceInfo = serviceInfoList[index];

            String device = currentServiceInfo.getPropertyString(SERVICE_INFO_PROPERTY_DEVICE);
            String ownDeviceId = ((MyApplication) context.getApplicationContext()).getDeviceId();

            if (!device.equals(ownDeviceId)) {
                String serverIpAddress = getIPv4FromServiceInfo(currentServiceInfo);
                ipAddressesSet.add(serverIpAddress);
            }
        }
        return ipAddressesSet;
    }

    public List<String> getOnlineDevicesList(Context context, String deviceId) {

        List<String> onlineDevices = new ArrayList<>();
        try {
            if (jmdns == null) {
                startDataTransferring(context);
            }

            ServiceInfo[] serviceInfoList = jmdns.list(SERVICE_INFO_TYPE);
            if (serviceInfoList != null) {

                for (int index = 0; index < serviceInfoList.length; index++) {
                    String device = serviceInfoList[index].getPropertyString(SERVICE_INFO_PROPERTY_DEVICE);

                    try {
                        if (!device.equals(deviceId)) {
                            String ip = getIPv4FromServiceInfo(serviceInfoList[index]);
                            if (!onlineDevices.contains(ip))
                                onlineDevices.add(ip);
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return onlineDevices;
    }


}
