package org.droidphy.core.network.jmdns;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;
import org.droidphy.core.utils.BroadcastUtil;

import java.io.IOException;

@EBean
public class SendToPeerTask {

    @Bean
    MessageSenderManager messageSenderManager;

    @Bean
    BroadcastUtil broadcastUtil;

    @Background
    void send(String ip, String message) {
        try {
            broadcast(messageSenderManager.getOrCreate(ip).send(message));
        } catch (IOException e) {
            broadcast("Exception on sending message");
        }
    }

    @UiThread
    void broadcast(String message) {
        broadcastUtil.sendBroadcast(message);
    }
}
