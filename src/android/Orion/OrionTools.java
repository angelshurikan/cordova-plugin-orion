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

    public static boolean applock = true;

    public static String listapplock = "";

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

        for (Method mMethod : mMethods) {
            if (mMethod.getName().equals("setWifiApEnabled")) {
                WifiConfiguration netConfig = new WifiConfiguration();
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
     *
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