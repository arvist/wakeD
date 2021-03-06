package com.cikoapps.deezeralarm.helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;


public class HelperClass {
    private static final String TAG = "HelperClass.java";
    private final Context context;

    public HelperClass(Context context) {
        this.context = context;
    }


     public static String getMonthFromInt(int month) {
        String[] monthNames = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        try {
            return monthNames[month];
        } catch (ArrayIndexOutOfBoundsException e){
            return null;
        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.UP);
        return bd.doubleValue();
    }

     public static String timeConversion(int totalSeconds) {

        final int MINUTES_IN_AN_HOUR = 60;
        final int SECONDS_IN_A_MINUTE = 60;

        int seconds = totalSeconds % SECONDS_IN_A_MINUTE;
        int totalMinutes = totalSeconds / SECONDS_IN_A_MINUTE;
        int minutes = totalMinutes % MINUTES_IN_AN_HOUR;
        int hours = totalMinutes / MINUTES_IN_AN_HOUR;
        if (hours > 0) {
            return hours + " h " + minutes + " min " + seconds + " sec";
        } else {
            return minutes + " min " + seconds + " sec";
        }
    }

     public static int randomInteger(int min, int max) {
        Random rand = new Random();
        return rand.nextInt((max - min)) + min;
    }

     public boolean isWifiConnected() {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }

    public boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    static public boolean allFalse(boolean[] values) {
        for (boolean value : values) {
            if (value)
                return false;
        }
        return true;
    }

    public boolean oneOrMoreTrue(boolean[] values) {
        for (boolean value : values) {
            if (value)
                return true;
        }
        return false;
    }

     @SuppressWarnings("deprecation")
    public boolean isLocationEnabled() {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }
}
