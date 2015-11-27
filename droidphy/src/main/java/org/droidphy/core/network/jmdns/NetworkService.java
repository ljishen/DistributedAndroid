package org.droidphy.core.network.jmdns;

import android.content.Context;
import android.net.wifi.WifiManager;
import com.orhanobut.logger.Logger;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.droidphy.core.R;
import org.droidphy.core.network.helpers.NetworkHelper;
import org.droidphy.core.utils.BroadcastUtil;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@EBean(scope = EBean.Scope.Singleton)
public class NetworkService {
    private String SERVICE_INFO_PROPERTY_HOST_ADDRESS = "HOST_ADDRESS";

    @Bean
    NetworkHelper networkHelper;

    @Bean
    MessageSenderManager messageSenderManager;

    @Bean
    MessageServer messageServer;

    @Bean
    BroadcastUtil broadcastUtil;

    private String appName;
    private String serviceInfoType;

    private ServiceInfo serviceInfo;

    private Set<String> peerIPs;
    private boolean peerIPsSlack;

    private JmDNS jmDNS;
    private boolean serviceRegistered;

    public NetworkService(Context context) {
        appName = context.getResources().getString(R.string.app_name);
        serviceInfoType = "_" + appName.toLowerCase() + "._http._tcp.local.";

        peerIPs = new HashSet<>();
    }

    private void init() throws IOException {
        if (jmDNS != null) {
            Logger.d("Service already initialized");
            return;
        }

        // Require permission android.permission.CHANGE_WIFI_MULTICAST_STATE
        networkHelper.getOrCreateMulticastLock(appName).acquire();

        Map<String, String> props = new HashMap<>();
        props.put(SERVICE_INFO_PROPERTY_HOST_ADDRESS, networkHelper.getLocalHostAddress());
        serviceInfo = ServiceInfo.create(serviceInfoType, appName, 8856, 0, 0, true, props);

        try {
            jmDNS = JmDNS.create(networkHelper.getLocalHost(), appName + "-JmDNS.local.");
        } catch (IOException e) {
            Logger.e(e, "Unable to create JmDNS");
            throw e;
        }

        jmDNS.addServiceListener(serviceInfoType, new ServiceListener() {
            public void serviceResolved(ServiceEvent event) {
                String peerIP = addPeerIP(peerIPs, event.getInfo());
                if (peerIP != null) {
                    Logger.d("ServiceResolved: " + peerIP);
                }
            }

            public void serviceRemoved(ServiceEvent event) {
                peerIPsSlack = true;
            }

            public void serviceAdded(ServiceEvent event) {
                jmDNS.requestServiceInfo(event.getType(), event.getName(), 2000);
            }
        });
    }

    @Background
    public void start() {
        try {
            messageServer.start();

            init();
            jmDNS.registerService(serviceInfo);
            serviceRegistered = true;

            Logger.d("NetworkService registered");
        } catch (IOException e) {
            Logger.e(e, "Fail to Register Service");
            broadcastUtil.sendBroadcast("Fail to Register Service. Please try again later.");
        }
    }

    @Background
    public void registerService() {
        if (serviceRegistered) {
            Logger.d("Service registered: ServiceInfo[%s]", serviceInfo);
            return;
        }

        start();
    }

    @Background
    public void unregisterService() {
        if (!serviceRegistered) {
            return;
        }

        jmDNS.unregisterService(serviceInfo);
        serviceRegistered = false;

        Logger.d("NetworkService unregistered");
    }

    @Background
    public void shutdown() {
        WifiManager.MulticastLock lock = networkHelper.getOrCreateMulticastLock(appName);
        if (lock.isHeld()) {
            lock.release();
        }

        try {
            if (jmDNS != null) {
                jmDNS.close();
                jmDNS = null;
            }

            messageServer.shutdown();

            Logger.d("NetworkService has shutdown");
        } catch (IOException e) {
            Logger.e(e, "Fail to Shutdown NetworkService");
        }
    }

    @Background
    public void sendToPeers(String message) {
        if (!serviceRegistered) {
            broadcastUtil.sendBroadcast("Please register service first!");
            return;
        }

        if (peerIPsSlack) {
            queryPeerIPs();
        }

        if (peerIPs.isEmpty()) {
            broadcastUtil.sendBroadcast("No peer found");
            return;
        }

        for (String ip : peerIPs) {
            try {
                broadcastUtil.sendBroadcast(messageSenderManager.getOrCreate(ip).send(message));
            } catch (IOException e) {
                broadcastUtil.sendBroadcast("Exception on sending message");
            }
        }
    }

    public Set<String> queryPeerIPs() {
        if (!serviceRegistered) {
            broadcastUtil.sendBroadcast("Please register service first!");
            return null;
        }

        Set<String> newPeerIPs = new HashSet<>();

        ServiceInfo[] serviceInfos = jmDNS.list(serviceInfoType);
        for (ServiceInfo info : serviceInfos) {
            addPeerIP(newPeerIPs, info);
        }
        peerIPs = newPeerIPs;
        peerIPsSlack = false;

        Logger.d("Peer IPs updated: %s", peerIPs);
        return peerIPs;
    }

    private String addPeerIP(Set<String> peerIPs, ServiceInfo info) {
        String hostAddress = info.getPropertyString(SERVICE_INFO_PROPERTY_HOST_ADDRESS);
        if (!hostAddress.equals(networkHelper.getLocalHostAddress())) {
            peerIPs.add(hostAddress);
            return hostAddress;
        } else {
            return null;
        }
    }
}
