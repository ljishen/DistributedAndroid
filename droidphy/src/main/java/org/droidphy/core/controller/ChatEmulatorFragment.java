package org.droidphy.core.controller;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import java.util.List;

import org.droidphy.core.R;
import org.droidphy.core.processor.transferring.DataTransferringManager;

public class ChatEmulatorFragment extends Fragment implements View.OnClickListener {

    private EditText edtTextForSending;
    private Button btnSend, btnChangeDevices, btnStart, btnStop;
    private DataTransferringManager dataTransferringManagerFromApplication;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.chat_emulator_layout, null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        edtTextForSending = (EditText) getActivity().findViewById(R.id.edtTextForSend);
        btnSend = (Button) getActivity().findViewById(R.id.btnSend);
        btnChangeDevices = (Button) getActivity().findViewById(R.id.btnShowDevicesList);
        btnStart = (Button) getActivity().findViewById(R.id.btnStartJmDns);
        btnStop = (Button) getActivity().findViewById(R.id.btnStopJmDns);

        btnSend.setOnClickListener(this);
        btnChangeDevices.setOnClickListener(this);
        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        startDataTransferring();
    }

    private void startDataTransferring() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                dataTransferringManagerFromApplication = ((MyApplication) getActivity().getApplication()).getDataTransferring();
                dataTransferringManagerFromApplication.startDataTransferring(getActivity());

            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnSend:
                processMessageSending();
                break;
            case R.id.btnShowDevicesList:
                onShowOnlineDevicesListDialogPress();
                break;
            case R.id.btnStartJmDns:
                dataTransferringManagerFromApplication.registerService();
                break;
            case R.id.btnStopJmDns:
                dataTransferringManagerFromApplication.unregisterService();
                break;

        }
    }

    public void onShowOnlineDevicesListDialogPress() {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String deviceId = ((MyApplication) getActivity().getApplication()).getDeviceId();
                final List<String> onlineDevices = dataTransferringManagerFromApplication.getOnlineDevicesList(getActivity(), deviceId);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showDevicesInNetworkList(onlineDevices);
                    }
                });
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();

    }

    private void processMessageSending() {
        final String inputText = edtTextForSending.getText().toString();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                dataTransferringManagerFromApplication.sendMessageToAllDevicesInNetwork(getActivity(), inputText);
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();

    }

    private void showDevicesInNetworkList(List<String> devices) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());

        builderSingle.setIcon(R.mipmap.ic_launcher);
        builderSingle.setTitle("Devices list");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.select_dialog_singlechoice);

        for (String device : devices) {
            arrayAdapter.add(device);
        }

        DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        };


        builderSingle.setAdapter(arrayAdapter, clickListener);
        builderSingle.show();
    }

}
