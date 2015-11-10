package org.droidphy.core.processor;

import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.droidphy.core.controller.MyApplication;
import org.droidphy.core.controller.helper.ToastHelper;

public class ClientProcessor {


    private Socket socket;
    private String textForSend;
    private Context context;
    private String serverIpAddress;

    public ClientProcessor(String textForSend, String serverIpAddress, Context context) {
        this.textForSend = textForSend;
        this.context = context;
        this.serverIpAddress = serverIpAddress;

    }

    public ClientProcessor(String serverIpAddress, Context context) {
        this.context = context;
        this.serverIpAddress = serverIpAddress;
    }

    private Socket getSocket(String serverIpAddress) {
        try {
            InetAddress serverAddr = InetAddress.getByName(serverIpAddress);
            socket = new Socket(serverAddr, ServerSocketProcessorRunnable.SERVER_PORT);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return socket;
    }

    private void closeSocket(Socket socket) {
        try {
            if (socket != null && !socket.isClosed())
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    ///////////////////////////////////////////////////////////////////////////

    public void sendTextToOtherDevice() {
        try {
            socket = getSocket(serverIpAddress);

            PrintWriter output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            output.println("MESSAGE FROM CLIENT");

            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String message = input.readLine();
            ToastHelper.doInUIThreadShort("Client received : " + message, MyApplication.currentContext);

            input.close();
            output.close();
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
            ToastHelper.doInUIThreadShort(e.getMessage(), MyApplication.currentContext);

        } finally {
            closeSocket(socket);
        }
    }

    public void sendSimpleMessageToOtherDevice(String message) {
        try {
            socket = getSocket(serverIpAddress);

            PrintWriter output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            output.println(message);

            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String messageFromClient = input.readLine();
            ToastHelper.doInUIThreadShort("Received answer : " + messageFromClient, MyApplication.currentContext);

            output.close();
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
            ToastHelper.doInUIThreadShort(e.getMessage(), MyApplication.currentContext);

        } finally {
            closeSocket(socket);
        }
    }


}
