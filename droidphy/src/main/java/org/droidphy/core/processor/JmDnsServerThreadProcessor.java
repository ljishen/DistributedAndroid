package org.droidphy.core.processor;

import android.content.Context;

import java.io.IOException;
import java.net.ServerSocket;

public class JmDnsServerThreadProcessor {

    private ServerSocket serverSocket = null;
    private Context context;
    private Thread serverProcessorThread;

    public void startServerProcessorThread(Context context) {
        this.context = context;
        ServerSocketProcessorRunnable serverSocketProcessor = new ServerSocketProcessorRunnable(serverSocket, context);
        this.serverProcessorThread = new Thread(serverSocketProcessor);
        this.serverProcessorThread.start();
    }

    public void stopServerProcessorThread() {

        try {
            // make sure you close the socket upon exiting
            if (serverSocket != null)
                serverSocket.close();

            if (serverProcessorThread != null)
                serverProcessorThread.interrupt();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
