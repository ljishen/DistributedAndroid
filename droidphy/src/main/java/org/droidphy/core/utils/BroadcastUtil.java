package org.droidphy.core.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import org.androidannotations.annotations.EBean;

@EBean
public class BroadcastUtil {
    public static final String BROADCAST_RECEIVER_ACTION_TOAST = "org.droidphy.ACTION_TOAST";
    public static final String BROADCAST_RECEIVER_EXTRA_MSG = "msg";

    private Context context;

    public BroadcastUtil(Context context) {
        this.context = context;
    }

    public void sendBroadcast(String message) {
        Intent intent = new Intent(BROADCAST_RECEIVER_ACTION_TOAST);
        intent.putExtra(
                BROADCAST_RECEIVER_EXTRA_MSG,
                message);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
