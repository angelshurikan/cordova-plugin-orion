package org.apache.cordova.orion;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.telephony.TelephonyManager;
import android.content.Context;

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
        if (action.equals("blockStatusBarOverlay")) {
            try {
                if (OrionTools.applock) {
                    callbackContext.error("Permission denied");
                } else if (!Settings.canDrawOverlays(cordova.getActivity())) {
                    Intent intent = new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION");
                    intent.setData(Uri.parse("package:" + cordova.getActivity().getPackageName()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    cordova.getActivity().startActivity(intent);
                    callbackContext.error("Permission denied");
                } else {
                    OrionStatusBarOverlay.add(cordova.getActivity());
                    callbackContext.success();
                }
            } catch (Exception e) {
                callbackContext.error(e.getMessage());
            }
        } else if (action.equals("checkConnectedDevices")) {
            //@description: get list of connected devices.
            try {
                JSONArray result = OrionTools.getConnectedDevices();
                System.out.println("Connected Devices success " + result);
                callbackContext.success(result);
            } catch (Exception e) {
                callbackContext.error(e.getMessage());
            }
        } else if (action.equals("checkHotspot")) {
            //@description: verify if hotspot is active.
            try {
                Context context = cordova.getActivity().getApplicationContext();
                boolean response = OrionTools.isWifiApEnabled(context);
                JSONObject r = new JSONObject();
                r.put("active", response);
                callbackContext.success(r);
            } catch (Exception e) {
                callbackContext.error(e.getMessage());
            }
            return true;
        } else if (action.equals("coolMethod")) {
            String message = args.getString(0);
            if (message != null && message.length() > 0) {
                callbackContext.success(message);
            } else {
                callbackContext.error("Expected one non-empty string argument.");
            }
            return true;
        } else if (action.equals("getApps")) {
            //@description: List of applications installed in the phone
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    try {
                        JSONArray array = getAppList();
                        callbackContext.success(array);
                    } catch (Exception e) {
                        callbackContext.error(e.getMessage());
                    }
                }
            });
            return true;
        } else if (action.equals("getBrightness")) {
            try {
                int mode = -1;
                mode = Settings.System.getInt(cordova.getActivity().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE); //this will return integer (0 or 1)
                int brightness = Settings.System.getInt(cordova.getActivity().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);  //returns integer value 0-255
                JSONObject r = new JSONObject();
                r.put("MODE", mode);
                r.put("BRIGHNESS", brightness);
                callbackContext.success(r);
            } catch (Settings.SettingNotFoundException e) {
                callbackContext.error(e.getMessage());
            }
            return true;
        } else if (action.equals("getCall")) {
            //@description: Call a phone number with the default phone application
            try {
                String number = OrionTools.parsePhoneNumber(args.getString(0));
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(number));
                //Bypass AppChooser
                intent.setPackage(OrionTools.getDialerPackage(intent, this));
                cordova.getActivity().startActivity(intent);
                callbackContext.success();
            } catch (NumberFormatException e) {
                callbackContext.error(e.getMessage());
            } catch (Exception e) {
                callbackContext.error(e.getMessage());
            }
            return true;
        } else if (action.equals("getInfo")) {
            JSONObject r = new JSONObject();
            r.put("MODEL", android.os.Build.MODEL);
            r.put("PRODUCT", android.os.Build.PRODUCT);
            r.put("MANUFACTURER", android.os.Build.MANUFACTURER);
            r.put("SERIAL", android.os.Build.SERIAL);
            r.put("IMEI", getImei());
            r.put("VERSION", getVersion());
            callbackContext.success(r);
            return true;
        } else if (action.equals("isDataActive")) {
            try {
                //@description: test if data is active
                Context context = cordova.getActivity().getApplicationContext();
                Boolean data = OrionTools.isDataActive(context);
                JSONObject r = new JSONObject();
                r.put("data", data);
                callbackContext.success(r);
            } catch (Exception e) {
                callbackContext.error(e.getMessage());
            }
        } else if (action.equals("permAccessibilityService")) {
            if (!isAccessibilitySettingsOn(cordova.getActivity().getApplicationContext())) {
                if (!OrionTools.applock) {
                    cordova.getActivity().startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                    callbackContext.success();
                } else {
                    callbackContext.error("Permission denied");
                }
            } else {
                callbackContext.success();
            }
        } else if (action.equals("setAppLock")) {
            try {
                Boolean applock = Boolean.parseBoolean(args.getString(0));
                String listapplock = args.getString(1);
                OrionTools.applock = applock;
                OrionTools.listapplock = listapplock;
                callbackContext.success();
            } catch (Exception e) {
                callbackContext.error(e.getMessage());
            }
        } else if (action.equals("setBrightness")) {
            try {
                Context context = cordova.getActivity().getApplicationContext();
                Class systemClass = Settings.System.class;
                Method canWriteMethod = systemClass.getDeclaredMethod("canWrite", Context.class);
                boolean retVal = (Boolean) canWriteMethod.invoke(null, cordova.getActivity());
                // verify write access
                if (retVal) {
                    Integer mode = Integer.parseInt(args.getString(0));
                    Integer brightness = Integer.parseInt(args.getString(1));
                    if (mode == 1) {
                        Settings.System.putInt(cordova.getActivity().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                    } else {
                        Settings.System.putInt(cordova.getActivity().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                        Settings.System.putInt(cordova.getActivity().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
                    }
                    callbackContext.success();
                } else {
                    if (OrionTools.applock) {
                        callbackContext.error("Permission denied");
                    } else {
                        Intent intent = new Intent("android.settings.action.MANAGE_WRITE_SETTINGS");
                        intent.setData(Uri.parse("package:" + cordova.getActivity().getPackageName()));
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        cordova.getActivity().startActivity(intent);
                        callbackContext.error("Permission denied");
                    }
                }
            } catch (Exception e) {
                callbackContext.error(e.getMessage());
            }
            return true;
        } else if (action.equals("setHotspot")) {
            //@description: set hotspot : setHotspot(ssid,psw,status)
            try {
                cordova.getThreadPool().execute(() -> {
                    try {
                        Context context = cordova.getActivity().getApplicationContext();
                        Class systemClass = Settings.System.class;
                        Method canWriteMethod = systemClass.getDeclaredMethod("canWrite", Context.class);
                        boolean retVal = (Boolean) canWriteMethod.invoke(null, cordova.getActivity());
                        // verify write access
                        if (retVal) {
                            String ssid = args.getString(0);
                            String pswd = args.getString(1);
                            Boolean status = Boolean.parseBoolean(args.getString(2));
                            //@description : set hotspot parameters
                            if (OrionTools.setHotSpot(ssid, pswd, context)) {
                                //@description : set hotspot status
                                if (OrionTools.startHotSpot(status, context)) {
                                    callbackContext.success();
                                    return;
                                }
                                callbackContext.error("Failed to set hotspot");
                                return;
                            }
                            callbackContext.error("Failed to configure hotspot SSID:" + ssid + ":" + pswd);
                            return;
                        } else {
                            if (OrionTools.applock) {
                                callbackContext.error("Permission denied");
                            } else {
                                Intent intent = new Intent("android.settings.action.MANAGE_WRITE_SETTINGS");
                                intent.setData(Uri.parse("package:" + cordova.getActivity().getPackageName()));
                                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                cordova.getActivity().startActivity(intent);
                                callbackContext.error("Permission denied");
                                return;
                            }
                        }
                    } catch (JSONException e) {
                        callbackContext.error(e.getMessage());

                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                        callbackContext.error(e.getMessage());
                    }
                });
            } catch (Exception e) {
                callbackContext.error(e.getMessage());
            }
            return true;
        }
        return false;
    }

    // Attention: API 26 getDeviceId(); => getImei();
    public String getImei() {
        Context context = this.cordova.getActivity().getApplicationContext();
        TelephonyManager tManager = (TelephonyManager) cordova.getActivity().getSystemService(context.TELEPHONY_SERVICE);
        return tManager.getDeviceId();
    }

    public String getVersion() {
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
    public JSONArray getAppList() throws JSONException {
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
            } catch (NameNotFoundException e) {
                Log.e("Orion::", "getAppList::" + e.getMessage());
            }
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
     *
     * @param event
     * @param json
     */
    public void fireEvent(final String event, JSONObject json) {
        final String str = json.toString();
        Log.d("Orion::Event", "Event: " + event + ", " + str);

        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String js = String.format("javascript:cordova.fireDocumentEvent(\"%s\", {\"data\":%s});", event, str);
                webView.loadUrl(js);
            }
        });
    }

    /**
     * @param mContext
     * @return
     * @description To check if service is enabled
     */
    private boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = cordova.getActivity().getPackageName() + "/" + OrionAccessibilityService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            //Log.d("Orion::", "AccessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e("Orion::", "Error finding setting, default accessibility to not found: " + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
        if (accessibilityEnabled == 1) {
            //Log.d("Orion::", "Accessibility is enabled");
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    //Log.d("Orion::", "accessibilityService:: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        //Log.d("Orion::", "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.d("Orion::", "Accessibility is disabled");
        }
        return false;
    }
}
