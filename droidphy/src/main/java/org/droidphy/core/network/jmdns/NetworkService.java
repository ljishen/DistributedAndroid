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
import org.droidphy.core.utils.FileUtil;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import java.io.IOException;
import java.util.*;

@EBean(scope = EBean.Scope.Singleton)
public class NetworkService {
    private String FILENAME_SERVICE_INSTANCE_NAME = "service_instance_name";
    private String SERVICE_INFO_PROPERTY_HOST_ADDRESS = "HOST_ADDRESS";

    @Bean
    NetworkHelper networkHelper;

    @Bean
    MessageServer messageServer;

    @Bean
    SendToPeerTask sendToPeerTask;

    @Bean
    BroadcastUtil broadcastUtil;

    @Bean
    FileUtil fileUtil;

    private String appName;
    private String serviceInfoType;

    private ServiceInfo serviceInfo;

    private Map<String, String> instanceNameToPeerIPs;
    private JmDNS jmDNS;

    private volatile ServiceStatus status;

    private enum ServiceStatus {
        REGISTERED, UNREGISTERED, REGISTERING
    }

    public NetworkService(Context context) {
        appName = context.getResources().getString(R.string.app_name);
        serviceInfoType = "_" + appName.toLowerCase(Locale.US) + "._http._tcp.local.";

        instanceNameToPeerIPs = new HashMap<>();
    }

    private void init() throws IOException {
        if (jmDNS != null) {
            Logger.d("Service already initialized");
            return;
        }

        // Require permission android.permission.CHANGE_WIFI_MULTICAST_STATE
        networkHelper.getOrCreateMulticastLock(appName).acquire();

        String serviceInstanceName = fileUtil.read(FILENAME_SERVICE_INSTANCE_NAME);
        if (serviceInstanceName == null) {
            serviceInstanceName = UUID.randomUUID().toString();
            fileUtil.write(serviceInstanceName, FILENAME_SERVICE_INSTANCE_NAME);
        }

        Map<String, String> props = new HashMap<>();
        props.put(SERVICE_INFO_PROPERTY_HOST_ADDRESS, networkHelper.getLocalHostAddress());
        serviceInfo = ServiceInfo.create(serviceInfoType, serviceInstanceName, 8856, 0, 0, true, props);

        try {
            jmDNS = JmDNS.create(networkHelper.getLocalHost(), appName + "-JmDNS.local.");
        } catch (IOException e) {
            throw new IOException("Unable to create JmDNS");
        }

        jmDNS.addServiceListener(serviceInfoType, new ServiceListener() {
            public void serviceResolved(ServiceEvent event) {
                String peerIP = addPeerIP(instanceNameToPeerIPs, event.getInfo());
                if (peerIP != null) {
                    Logger.d("ServiceResolved: " + peerIP);
                }
            }

            public void serviceRemoved(ServiceEvent event) {
                String peerIP = instanceNameToPeerIPs.remove(event.getName());
                Logger.d("ServiceRemoved: " + peerIP);
            }

            public void serviceAdded(ServiceEvent event) {
                jmDNS.requestServiceInfo(event.getType(), event.getName(), 2000);
            }
        });
    }

    @Background
    public void registerService() {
        if (status == ServiceStatus.REGISTERED ||
                status == ServiceStatus.REGISTERING) {
            Logger.d("Service registered or registering");
            return;
        }
        status = ServiceStatus.REGISTERING;

        try {
            messageServer.start();

            init();
            jmDNS.registerService(serviceInfo);
            status = ServiceStatus.REGISTERED;

            Logger.d("NetworkService registered");
        } catch (IOException e) {
            status = ServiceStatus.UNREGISTERED;
            Logger.e(e, "Fail to Register Service");
            broadcastUtil.sendBroadcast("Fail to Register Service. Please try again later.");
        }
    }

    @Background
    public void unregisterService() {
        if (status != ServiceStatus.REGISTERED) {
            return;
        }

        jmDNS.unregisterService(serviceInfo);
        status = ServiceStatus.UNREGISTERED;

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
                status = ServiceStatus.UNREGISTERED;
                jmDNS = null;
            }

            messageServer.shutdown();

            Logger.d("NetworkService has shutdown");
        } catch (IOException e) {
            Logger.e(e, "Fail to Shutdown NetworkService");
        }
    }

    public void sendToPeers(String message) {
        if (status != ServiceStatus.REGISTERED) {
            broadcastUtil.sendBroadcast("Please register service first!");
            return;
        }

        if (instanceNameToPeerIPs.isEmpty()) {
            broadcastUtil.sendBroadcast("No peer found");
            return;
        }

        for (String ip : instanceNameToPeerIPs.values()) {
            sendToPeerTask.send(ip, message);
        }
    }

    public Collection<String> queryPeerIPs() {
        if (status != ServiceStatus.REGISTERED) {
            broadcastUtil.sendBroadcast("Please register service first!");
            return null;
        }

        Map<String, String> newInstanceNameToPeerIPs = new HashMap<>();

        ServiceInfo[] serviceInfos = jmDNS.list(serviceInfoType);
        for (ServiceInfo info : serviceInfos) {
            addPeerIP(newInstanceNameToPeerIPs, info);
        }
        instanceNameToPeerIPs = newInstanceNameToPeerIPs;

        Logger.d("Peer IPs updated: %s", instanceNameToPeerIPs);
        return instanceNameToPeerIPs.values();
    }

    private String addPeerIP(Map<String, String> instanceNameToPeerIPs, ServiceInfo info) {
        String hostAddress = info.getPropertyString(SERVICE_INFO_PROPERTY_HOST_ADDRESS);
        if (!hostAddress.equals(networkHelper.getLocalHostAddress())) {
            instanceNameToPeerIPs.put(info.getName(), hostAddress);
            return hostAddress;
        } else {
            return null;
        }
    }
}
