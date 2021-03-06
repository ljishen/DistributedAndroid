package org.droidphy.core.network.jmdns;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Closer;
import com.orhanobut.logger.Logger;
import org.androidannotations.annotations.EBean;
import org.droidphy.core.activities.DroidphyApplication;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

@EBean(scope = EBean.Scope.Singleton)
public class MessageSenderManager {
    private static final int SOCKET_TIMEOUT = 2 * 1000;

    private Map<String, MessageSender> senders;

    public MessageSenderManager() {
        senders = new HashMap<>();
    }

    /**
     * Note that this method allow creating more then one {@link MessageSender}
     * when multiple threads call at the same time.
     */
    public MessageSender getOrCreate(String ip) {
        MessageSender sender = senders.get(ip);
        if (sender == null) {
            sender = new MessageSender(ip);
            senders.put(ip, sender);
        }
        return sender;
    }

    public static final class MessageSender {
        private String ip;

        private MessageSender(String ip) {
            this.ip = ip;
        }

        public String send(String message) throws IOException {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, MessageServer.SERVER_SOCKET_PORT), SOCKET_TIMEOUT);
            socket.setSoTimeout(SOCKET_TIMEOUT);

            Closer closer = Closer.create();
            try {
                Writer writer = closer.register(
                        new BufferedWriter(
                                new OutputStreamWriter(socket.getOutputStream(), Charsets.UTF_8),
                                DroidphyApplication.BUFFER_SIZE));
                Reader r = closer.register(new StringReader(message));
                CharStreams.copy(r, writer);
                writer.flush();
                socket.shutdownOutput();

                BufferedReader reader = closer.register(
                        new BufferedReader(
                                new InputStreamReader(socket.getInputStream(), Charsets.UTF_8),
                                DroidphyApplication.BUFFER_SIZE));
                return CharStreams.toString(reader);
            } catch (IOException e) {
                Logger.e(e, "Fail to communicate on Socket[%s]", socket);
                throw closer.rethrow(e);
            } finally {
                closer.close();
            }
        }
    }
}
