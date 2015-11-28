package org.droidphy.core.network.jmdns;

import android.content.Context;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Closer;
import com.google.common.util.concurrent.MoreExecutors;
import com.orhanobut.logger.Logger;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.droidphy.core.activities.DroidphyApplication;
import org.droidphy.core.utils.BroadcastUtil;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@EBean(scope = EBean.Scope.Singleton)
public class MessageServer {
    public static final int SERVER_SOCKET_PORT = 8700;

    @RootContext
    Context context;

    private ExecutorService executor;
    private boolean running;

    private ServerSocket serverSocket;

    public void start() throws IOException {
        if (running) {
            Logger.d("MessageServer is running");
            return;
        }

        serverSocket = new ServerSocket(SERVER_SOCKET_PORT);
        executor = Executors.newCachedThreadPool();

        Runnable connectionHandler = new Runnable() {
            @Override
            public void run() {
                while (running) {
                    try {
                        executor.execute(new SocketHandler(context, serverSocket.accept()));
                    } catch (IOException e) {
                        if (!serverSocket.isClosed()) {
                            Logger.e(e, "Fail to accept connection ServiceSocket[%s]", serverSocket);
                        }
                    }
                }
            }
        };

        executor.execute(connectionHandler);
        running = true;
    }

    public void shutdown() throws IOException {
        if (!running) {
            return;
        }

        running = false;

        MoreExecutors.shutdownAndAwaitTermination(executor, 6, TimeUnit.SECONDS);
        serverSocket.close();
    }

    private static final class SocketHandler implements Runnable {
        private Context context;
        private Socket socket;

        SocketHandler(Context context, Socket socket) {
            this.context = context;
            this.socket = socket;
        }

        @Override
        public void run() {
            Closer closer = Closer.create();
            try {
                BufferedReader reader = closer.register(
                        new BufferedReader(
                                new InputStreamReader(socket.getInputStream(), Charsets.UTF_8),
                                DroidphyApplication.BUFFER_SIZE));
                String incomeMsg = CharStreams.toString(reader);
                new BroadcastUtil(context).sendBroadcast(incomeMsg);

                Writer writer = closer.register(
                        new BufferedWriter(
                                new OutputStreamWriter(socket.getOutputStream(), Charsets.UTF_8),
                                DroidphyApplication.BUFFER_SIZE));
                Reader r = closer.register(new StringReader("Message Received \"" + incomeMsg + "\""));
                CharStreams.copy(r, writer);
                writer.flush();
                socket.shutdownOutput();
            } catch (IOException e) {
                Logger.e(e, "Fail to communicate on Socket[%s]", socket);
            } finally {
                try {
                    closer.close();
                } catch (IOException e) {
                    Logger.e(e, "Fail to close resources");
                }
                context = null;
            }
        }
    }
}
