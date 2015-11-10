package org.droidphy.core.controller;

import android.app.Application;
import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import org.droidphy.core.processor.transferring.DataTransferringManager;

public class MyApplication extends Application {

    private DataTransferringManager dataTransferring;
    private String deviceId = "";
    public static Context currentContext;

    public DataTransferringManager getDataTransferring() {
        if (dataTransferring == null)
            dataTransferring = new DataTransferringManager();
        return dataTransferring;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm.getDeviceId() != null) {
            setDeviceId(tm.getDeviceId()); //use for mobiles
        } else {
            setDeviceId(Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID)); //use for tablets
        }

        dataTransferring = new DataTransferringManager();

    }

    private void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceId() {
        return deviceId;
    }

}
