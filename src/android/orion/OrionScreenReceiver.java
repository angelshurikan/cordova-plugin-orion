package org.apache.cordova.orion;


import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.Context;


/**
 * Intent receiver for screen turned on/off
 */
public class OrionScreenReceiver  extends BroadcastReceiver {
    public static boolean screenOff;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            screenOff = true;
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            screenOff = false;
        }
        Intent i = new Intent(context, OrionScreenService.class);
        i.putExtra("screen_state", screenOff);
        context.startService(i);
    }
}