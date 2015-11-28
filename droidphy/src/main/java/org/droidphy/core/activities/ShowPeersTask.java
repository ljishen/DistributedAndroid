package org.droidphy.core.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.ArrayAdapter;
import org.androidannotations.annotations.*;
import org.droidphy.core.network.jmdns.NetworkService;

import java.util.Collection;

@EBean
public class ShowPeersTask {

    @RootContext
    Activity activity;

    @Bean
    NetworkService networkService;

    @Background
    void queryAndShow() {
        Collection<String> peers = networkService.queryPeerIPs();
        if (peers != null) {
            show(peers);
        }
    }

    @UiThread
    void show(Collection<String> peers) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(activity);
        builderSingle.setTitle("Peers");

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(
                activity,
                android.R.layout.select_dialog_singlechoice);

        for (String peer : peers) {
            adapter.add(peer);
        }

        DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO: Choose target to send message
            }
        };

        builderSingle.setAdapter(adapter, clickListener);
        builderSingle.show();
    }
}
