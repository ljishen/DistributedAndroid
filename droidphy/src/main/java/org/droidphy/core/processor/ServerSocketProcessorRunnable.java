package org.droidphy.core.processor;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.droidphy.core.controller.MyApplication;
import org.droidphy.core.controller.helper.ToastHelper;

public class ServerSocketProcessorRunnable implements Runnable {

    public static final int SERVER_PORT = 8700;
    private ServerSocket serverSocket;
    private Context context;
    private BufferedReader inputBufferedReader;
    private PrintWriter outputPrintWriter;


    private static String TAG = "SERVER_SOCKET";

    public ServerSocketProcessorRunnable(ServerSocket serverSocket, Context context) {
        this.serverSocket = serverSocket;
        this.context = context;
    }

    public void run() {
        Socket socket;
        try {
            serverSocket = new ServerSocket(SERVER_PORT);


        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            while (!Thread.currentThread().isInterrupted()) {

                Log.v(TAG, "before socket ACCEPT");
                socket = serverSocket.accept();
                Log.v(TAG, "ACCEPTED");


                InputStream inputStream = socket.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                inputBufferedReader = new BufferedReader(inputStreamReader);

//                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
//                BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
//                PrintWriter printWriter = new PrintWriter(bufferedWriter, true);
//                printWriter.println("some info");

                outputPrintWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                processInputInputOutputBuffers();
            }


            inputBufferedReader.close();
            outputPrintWriter.close();

            Log.v(TAG, "BUFFERS CLOSED");


        } catch (Exception ex) {
            Log.v(TAG, "server socket processor thread EXCEPTION : " + ex.toString());

        } catch (Error error) {
            Log.v(TAG, "server socket processor thread ERROR : " + error.toString());
        }

    }


    private void processInputInputOutputBuffers() throws IOException {

        Log.v(TAG, "...SOCKET DATA PROCESSING...");

        String inputLine = inputBufferedReader.readLine();
        if (inputLine != null) {
            ToastHelper.doInUIThreadShort("Received message : " + inputLine, MyApplication.currentContext);
            outputPrintWriter.println("YOU TEXT ARRIVED. THANKS");

        }


    }


}
