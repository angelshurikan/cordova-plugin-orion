package org.apache.cordova.orion;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.telephony.TelephonyManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageManager;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;



/**
 * This class echoes a string called from JavaScript.
 */
public class Orion extends CordovaPlugin {
    private final boolean BETA = false;

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if(action.equals("checkConnectedDevices")){
            //@description: get list of connected devices.
            try{
                JSONArray result = OrionTools.getConnectedDevices();
                System.out.println("Connected Devices success " + result);
                callbackContext.success(result);
            }catch (Exception e){
                System.err.println("Error ::::: " + e);
                callbackContext.error("Failed to get connected devices.");
            }
        } else if(action.equals("checkHotspot")){
            //@description: verify if hotspot is active.
            try{
                Log.d("Orion::Hotspot::", "checking");
                Context context= cordova.getActivity().getApplicationContext();
                boolean response = OrionTools.isWifiApEnabled(context);
                JSONObject r = new JSONObject();
                r.put("active", response);
                callbackContext.success(r);
            }catch(Exception e){
                Log.e("Orion::Hotspot::",  e.getMessage());
                callbackContext.error("Failed checking hotspot");
            }
            return true;
        } else if(action.equals("coolMethod")) {
            String message = args.getString(0);
            coolMethod(message, callbackContext);
            return true;
        } else if(action.equals("getInfo")) {
            JSONObject r = new JSONObject();
            r.put("TEST", "ok");
            r.put("MODEL", android.os.Build.MODEL);
            r.put("PRODUCT", android.os.Build.PRODUCT);
            r.put("MANUFACTURER", android.os.Build.MANUFACTURER);
            r.put("SERIAL", android.os.Build.SERIAL);
            r.put("IMEI", this.getImei());
            r.put("VERSION", this.getVersion());
            callbackContext.success(r);
            return true;
        } else if(action.equals("getApps")) {
            //@description: Liste des apps
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    try {
                        JSONArray array = getAppList();
                        callbackContext.success(array);
                    } catch (Exception e) {
                        System.err.println("Exception: " + e.getMessage());
                        callbackContext.error(e.getMessage());
                    }
                }
            });
            return true;
        } else if(action.equals("isDataActive")){
            try{
                //@description: test if data is active
                Context context= cordova.getActivity().getApplicationContext();
                Boolean data = OrionTools.isDataActive(context);
                JSONObject r = new JSONObject();
                r.put("data", data);
                callbackContext.success(r);
            }catch (Exception e){
                callbackContext.error("Failed to test data");
            }
        } else if(action.equals("getCall")) {
            //@description: Make a phone call.
            try {

                String number = OrionTools.parsePhoneNumber(args.getString(0));
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(number));
                //Bypass AppChooser
                intent.setPackage(OrionTools.getDialerPackage(intent, this));
                cordova.getActivity().startActivity(intent);
                Log.d("Orion::getCall::", "Calling :: " + number);
                callbackContext.success();
            } catch (NumberFormatException e) {
                Log.d("Orion::getCall::", "Call bad number ::" + args.getString(0)  );
                callbackContext.error("Bad Number");
            }catch (Exception e){
                Log.d("Orion::getCall::", "Call failed" + e.getMessage() );
                callbackContext.error("Failed :" + e.getMessage());
            }
            return true;
        } else if(action.equals("launchService")){
            //@description: launch orion background services.
            try {
                Context context = cordova.getActivity().getApplicationContext();
                //@description: Screen Activated Service
                Intent serviceIntent = new Intent(context,OrionScreenService.class);
                context.startService(serviceIntent);
                Log.d("Orion::", "ScreenService Active");
                callbackContext.success();
            }catch(Exception e){
                Log.e("Orion::Service::", e.getMessage());
                callbackContext.error("Failed launching Orion Services");
            }
            return true;
        } else if(action.equals("setHotspot")){
            //@description: set hotspot : setHotspot(ssid,psw,status)
            try{
                Log.d("Orion::Hotspot::", "toggling");
                cordova.getThreadPool().execute(() -> {
                    try{
                        Context context = cordova.getActivity().getApplicationContext();
                        Class systemClass = Settings.System.class;
                        Method canWriteMethod = systemClass.getDeclaredMethod("canWrite", Context.class);
                        boolean retVal = (Boolean) canWriteMethod.invoke(null, cordova.getActivity());
                        // verify write access
                        if(retVal){
                            String ssid = args.getString(0);
                            String pswd = args.getString(1);
                            Boolean status = Boolean.parseBoolean(args.getString(2));
                            //@description : set hotspot parameters
                            if(OrionTools.setHotSpot(ssid,pswd,context)){
                                //@description : set hotspot status
                                if(OrionTools.startHotSpot(status, context)){
                                    callbackContext.success();
                                    return;
                                }
                                callbackContext.error("Failed to set hotspot");
                                return;
                            }
                            callbackContext.error("Failed to configure hotspot SSID:" + ssid + ":" + pswd);
                            return;
                        }else {
                            Log.d("Orion::Hotspot::", "asking permissions");
                            Intent intent = new Intent("android.settings.action.MANAGE_WRITE_SETTINGS");
                            intent.setData(Uri.parse("package:" + cordova.getActivity().getPackageName()));
                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            cordova.getActivity().startActivity(intent);
                            callbackContext.success();
                            callbackContext.error("Permission denied");
                            return;
                        }
                    }catch (JSONException e){
                        callbackContext.error("Bad arguments");

                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
                        callbackContext.error("Hotspot interface error");
                    }
                });
            }catch (Exception e){
                Log.e("Orion::Hotspot::", " Hotspot failed to toggle" + e.getMessage());
                callbackContext.error(" Hotspot failed to toggle" + e.getMessage());
            }
            return true;
        }
        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    // Attention: API 26 getDeviceId(); => getImei();
    private String getImei() {
        Context context = this.cordova.getActivity().getApplicationContext();
        TelephonyManager tManager = (TelephonyManager) cordova.getActivity().getSystemService(context.TELEPHONY_SERVICE);
        return tManager.getDeviceId();
    }

    private String getVersion() {
        try {
            PackageManager packageManager = this.cordova.getActivity().getPackageManager();
            return packageManager.getPackageInfo(this.cordova.getActivity().getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            return "N/A";
        }
    }

    /**
     * Get an array containg all the apps installed.
     *
     * @return JSONArray containing a list of Apps :
     * - id : the app id (package name)
     * - name : the app name (label name)
     * - img : the app icon path.
     * - logo : the icon name.
     */
    private JSONArray getAppList() throws JSONException {
        final PackageManager pm = cordova.getActivity().getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resInfos = pm.queryIntentActivities(intent, 0);
        // Eliminate duplicates by using hashset
        HashSet<String> packageNames = new HashSet<String>(0);
        List<ApplicationInfo> appInfos = new ArrayList<ApplicationInfo>(0);

        //getting package names and adding them to the hashset
        for (ResolveInfo resolveInfo : resInfos) {
            packageNames.add(resolveInfo.activityInfo.packageName);
        }

        for (String packageName : packageNames) {
            try {
                appInfos.add(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
            } catch (NameNotFoundException e) {   }
        }
        //sort the list of apps by their names
        Collections.sort(appInfos, new ApplicationInfo.DisplayNameComparator(pm));

        JSONArray app_list = new JSONArray();
        int cnt = 0;
        String path = OrionTools.getDataPath(this);
        OrionTools.makeRootDirectory(path + "/Applist/");
        for (ApplicationInfo packageInfo : appInfos) {
            JSONObject info = new JSONObject();
            info.put("id", packageInfo.packageName);
            info.put("name", packageInfo.loadLabel(pm));
            String img_name = "/Applist/" + packageInfo.packageName + ".png";
            info.put("img", path + img_name);
            info.put("logo", img_name);
            File cheakfile = new File(path + img_name);
            if (!cheakfile.exists()) {
                Drawable icon = pm.getApplicationIcon(packageInfo);
                if (icon != null) {
                    OrionTools.drawableTofile(icon, path + img_name);
                }
            }
            app_list.put(cnt++, info);
        }
        return app_list;
    }

    /**
     * Fires a javascript event.
     * @param event
     * @param json
     */
    public void fireEvent(final String event, JSONObject json) {
        final String str = json.toString();
        Log.d("Orion::Event", "Event: " + event + ", " + str);

        cordova.getActivity().runOnUiThread(new Runnable(){
            @Override
            public void run() {
                String js = String.format("javascript:cordova.fireDocumentEvent(\"%s\", {\"data\":%s});", event, str);
                webView.loadUrl( js );
            }
        });
    }
}
