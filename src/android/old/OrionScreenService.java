package org.apache.cordova.orion;

import android.content.Intent;
import android.app.Service;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.os.IBinder;
import android.util.Log;

/**
 * Backgroung Service for Orion
 */
public class OrionScreenService extends Service {
    private static OrionScreenService self = null;

    @Override
    public void onCreate() {
        self = this;
        super.onCreate();
        //@description: register receiver that handles screen on and screen off action
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        BroadcastReceiver mReceiver = new OrionScreenReceiver();
        registerReceiver(mReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //@description: check for screen status
        boolean screenOff = intent.getBooleanExtra("screen_state", false);
        if (screenOff) {
            //@description: screen off : launch Elysium on top
            Log.d("Orion::ScreenService::","Launching elysium");
            Boolean d = OrionTools.openApp(this, "fr.mylocalphone.elysium");
            if(!d) Log.d("Orion::ScreenService::","Elysium launch failed");
        } else {
            //Screen On
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy(){
        self = null;
        super.onDestroy();
    }

    public static boolean isAlive(){
        return self != null;
    }
}