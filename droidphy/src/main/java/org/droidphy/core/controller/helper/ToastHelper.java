package org.droidphy.core.controller.helper;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import java.util.Iterator;
import java.util.Set;

public class ToastHelper {

    public static void useLongToast(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);

//        View view = toast.getView();
//        view.setBackgroundResource(R.color.col_blue_my);

        toast.show();
    }

    public static void useShortToast(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);

//        View view = toast.getView();
//        view.setBackgroundResource(R.color.col_blue_my);

        toast.show();
    }

    public static void doInUIThread(final String message, final Context context) {
        Activity act = (Activity) context;
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastHelper.useLongToast(context, message);
            }
        });
    }

    public static void doInUIThreadShort(final String message, final Context context) {
        Activity act = (Activity) context;
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastHelper.useShortToast(context, message);
            }
        });
    }

    public static void useLongToastForIntegerSet(Set<Integer> ourSet, Context context) {

        String pringStr = "Set : ";

        for (Integer e : ourSet) {
            pringStr += e.toString() + ", ";
        }
        useLongToast(context, pringStr);
    }


    /*
    USE THIS FOR useToastInService method.
    private Handler handler;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler = new Handler();
        return super.onStartCommand(intent, flags, startId);
    }
     */

    public static void useToastInService(final Handler handler, final Context context, final String message) {

        handler.post(new Runnable() {
            @Override
            public void run() {
                useLongToast(context, message);
            }
        });
    }

}
