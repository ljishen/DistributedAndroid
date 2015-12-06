package org.droidphy.core.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import org.androidannotations.annotations.*;
import org.droidphy.core.R;
import org.droidphy.core.network.jmdns.NetworkService;
import org.droidphy.core.utils.BroadcastUtil;
import org.droidphy.core.utils.ToastUtil;

@EActivity(R.layout.activity_main)
public class MainActivity extends Activity {

    @Bean
    ToastUtil toastUtil;

    @Bean
    NetworkService networkService;

    @NonConfigurationInstance
    @Bean
    ShowPeersTask showPeersTask;

    @ViewById(R.id.edit_text_message)
    EditText editTextMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        networkService.registerService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        networkService.shutdown();
    }

    @Click(R.id.btn_register_service)
    void btnRegisterServiceClicked() {
        networkService.registerService();
    }

    @Click(R.id.btn_unregister_service)
    void btnUnregisterServiceClicked() {
        networkService.unregisterService();
    }

    @Click(R.id.btn_show_peers)
    void btnShowPeersClicked() {
        showPeersTask.queryAndShow();
    }

    @Click(R.id.btn_send)
    void btnSendClicked() {
        String message = editTextMessage.getText().toString();
        networkService.sendToPeers(message);
    }

    @Receiver(actions = BroadcastUtil.BROADCAST_RECEIVER_ACTION_TOAST, local = true)
    void onActionMessage(
            @Receiver.Extra(BroadcastUtil.BROADCAST_RECEIVER_EXTRA_MSG)
            String message) {
        toastUtil.show(message, Toast.LENGTH_SHORT);
    }
}