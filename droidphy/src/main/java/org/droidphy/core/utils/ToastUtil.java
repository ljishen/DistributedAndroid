package org.droidphy.core.utils;

import android.app.Activity;
import android.widget.Toast;
import com.orhanobut.logger.Logger;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

@EBean
public class ToastUtil {

    @RootContext
    Activity activity;

    public void show(final CharSequence text, final int duration) {
        if (activity != null) {
            Toast.makeText(activity, text, duration).show();
        } else {
            Logger.w("Unable to toast message: " + text);
        }
    }
}
