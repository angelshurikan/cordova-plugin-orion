package org.apache.cordova.orion;

import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ActivityNotFoundException;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;
import org.apache.cordova.CordovaPlugin;

import java.io.*;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.os.Environment;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.content.Context;
import android.graphics.Canvas;
import android.util.Base64;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

import android.telephony.TelephonyManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class OrionTools {

    // Attention: API 26 getDeviceId(); => getImei();
    public static String getImei() {
        Context context = this.cordova.getActivity().getApplicationContext();
        TelephonyManager tManager = (TelephonyManager) cordova.getActivity().getSystemService(context.TELEPHONY_SERVICE);
        return tManager.getDeviceId();
    }

    public static String getVersion() {
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
    public static void fireEvent(final String event, JSONObject json) {
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

    /**
     * Create an image file from a drawable icon.
     *
     * @param drawable
     * @param path
     */
    public static void drawableTofile(Drawable iconDrawable, String path) {
        File file = new File(path);
//            Bitmap bitmap=((BitmapDrawable)drawable).getBitmap();
//            ByteArrayOutputStream bos = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100 /*ignored for PNG*/, bos);
//            byte[] bitmapdata = bos.toByteArray();

        Bitmap bitmap = Bitmap.createBitmap(iconDrawable.getIntrinsicWidth(),
                iconDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        iconDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        iconDrawable.draw(canvas);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] bitmapdata = byteArrayOutputStream.toByteArray();
//            return "data:image/png;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT);

        //write the bytes in file
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file);
            fos.write(bitmapdata);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the datapath of current plugin.
     *
     * @param plugin
     * @return
     */
    public static String getDataPath(CordovaPlugin plugin) {
        Context context = plugin.cordova.getActivity().getApplicationContext();
        return context.getFilesDir().getPath();
    }

    /**
     * Create a directory.
     *
     * @param filePath
     */
    public static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if telephone is enabled.
     *
     * @param plugin
     * @return
     */
    public static boolean isTelephonyEnabled(CordovaPlugin plugin) {
        TelephonyManager tm = (TelephonyManager) plugin.cordova.getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        return tm != null && tm.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE;
    }

    /**
     * Get default phone app.
     *
     * @param intent
     * @param plugin
     * @return
     */
    public static String getDialerPackage(Intent intent, CordovaPlugin plugin) {
        PackageManager packageManager = (PackageManager) plugin.cordova.getActivity().getPackageManager();
        List activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        for (int i = 0; i < activities.size(); i++) {
            if (activities.get(i).toString().toLowerCase().contains("com.android.server.telecom")) {
                return "com.android.server.telecom";
            }
            if (activities.get(i).toString().toLowerCase().contains("com.android.phone")) {
                return "com.android.phone";
            } else if (activities.get(i).toString().toLowerCase().contains("call")) {
                return activities.get(i).toString().split("[ ]")[1].split("[/]")[0];
            }
        }
        return "";
    }

    /**
     * Test Number format
     *
     * @param number
     * @return number
     */
    public static String parsePhoneNumber(String number) throws NumberFormatException {
        if (!number.startsWith("tel:")) {
            number = String.format("tel:%s", number);
        }
        number = number.replaceAll("#", "%23");
        return number;
    }

    /**
     * Launches an App
     *
     * @param context
     * @param packageName
     * @return
     */
    public static boolean openApp(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();
        try {
            Intent i = manager.getLaunchIntentForPackage(packageName);
            if (i == null) {
                return false;
            }
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            context.startActivity(i);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }

    /**
     * set Hotspot On/Off
     *
     * @param enable   true = on, false = off
     * @param mContext current context
     * @return
     */
    public static boolean startHotSpot(boolean enable, Context mContext) {
        WifiManager mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        Method[] mMethods = mWifiManager.getClass().getDeclaredMethods();
        for (Method mMethod : mMethods) {
            if (mMethod.getName().equals("setWifiApEnabled")) {
                try {
                    mMethod.invoke(mWifiManager, null, enable);
                    return true;
                } catch (Exception ex) {
                    Log.e("Orion::Hotspot::", "Unknown error during hotspot creation.", ex);
                }
                break;
            }
        }
        return false;
    }

    /**
     * Test if Hotspot is enabled.
     *
     * @param mContext
     * @return
     */
    public static boolean isWifiApEnabled(Context mContext) {
        try {
            WifiManager mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            Method method = mWifiManager.getClass().getMethod("isWifiApEnabled");
            return (Boolean) method.invoke(mWifiManager);
        } catch (Exception e) {
            Log.e("Orion::Hotspot::", "Unknown error while checking ap wifi.", e);
        }
        return false;
    }

    /**
     * Configure hotspot
     *
     * @param SSID     hotspot name
     * @param passWord hotspot password
     * @param mContext
     * @return // Default security protocol WPA_PSK
     */
    public static boolean setHotSpot(String SSID, String passWord, Context mContext) {

        WifiManager mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
        if (list != null) {
            for (WifiConfiguration i : list) {
                if (i.SSID != null && i.SSID.equals(SSID)) {
                    mWifiManager.disconnect();
                    mWifiManager.removeNetwork(i.networkId);
                    mWifiManager.saveConfiguration();
                    break;
                }
            }
        }

        if (SSID == null || passWord == null) {
            Log.e("Orion::APNetwork::", "Please provide a SSID and a password");
            return false;
        }

        Method[] mMethods = mWifiManager.getClass().getDeclaredMethods();

        Log.v("Orion::APNetwork::", "Creating hotspot");
        for (Method mMethod : mMethods) {
            if (mMethod.getName().equals("setWifiApEnabled")) {
                WifiConfiguration netConfig = new WifiConfiguration();

                Log.i("Orion::APNetwork::", "Applying hotspot settings with security: WPA");
                netConfig.SSID = SSID;
                netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                netConfig.SSID = SSID;
                netConfig.preSharedKey = passWord;
                netConfig.hiddenSSID = false;
                netConfig.status = WifiConfiguration.Status.ENABLED;
                netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    netConfig.allowedKeyManagement.set(4); // WPA2_PSK on Android 4+!
                } else {
                    netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                }
                netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);

                try {
                    mMethod.invoke(mWifiManager, netConfig, true);
                    mWifiManager.disconnect();
                    mWifiManager.reconnect();
                    mWifiManager.saveConfiguration();
                    Log.v("Orion::APNetwork::", "Successfully created hotspot");
                    return true;

                } catch (Exception e) {
                    Log.e("Orion::APNetwork::", "Unknown error during saving wifi config.", e);
                }
            }
        }
        return false;
    }

    /**
     * Get list of MAC/IP adresses of connected devices.
     *
     * @return
     */
    public static JSONArray getConnectedDevices() {
        JSONArray list = new JSONArray();

        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] sp = line.split(" +");
                JSONObject obj = new JSONObject();
                if (sp[3].matches("..:..:..:..:..:..")) {
                    obj.put("mac", sp[3]);
                }
                obj.put("ip", sp[0]);
                list.put(obj);
            }
            br.close();
        } catch (IOException e) {
            Log.e("Orion::GETMACADDR::", e.getMessage());
        } catch (JSONException e) {
            Log.e("Orion::getUsers::", e.getMessage());
        }
        return list;
    }

    /**
     * Test if mobile data is active
     * @param context
     * @return
     */
    public static boolean isDataActive(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            Class cmClass = Class.forName(cm.getClass().getName());
            Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true); // Make the method callable
            return (Boolean) method.invoke(cm);
        } catch (Exception e) {
            Log.e("Orion::DataQuery::", e.getMessage());
            return false;
        }
    }
}