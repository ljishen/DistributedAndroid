package org.droidphy.core.activities;

import android.app.Application;
import com.orhanobut.logger.Logger;
import org.androidannotations.annotations.EApplication;

@EApplication
public class DroidphyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.init().methodCount(1).hideThreadInfo();
    }
}
