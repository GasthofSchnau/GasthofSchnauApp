package de.gasthof_schnau.gasthofschnau.lib;

import android.content.Context;
import android.net.ConnectivityManager;

public class Internet {

    public static boolean isOnline(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connMgr.getActiveNetworkInfo() != null && connMgr.getActiveNetworkInfo().isConnected();
    }

    public static boolean isWiFiConnected(Context c) {
        ConnectivityManager connMgr = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connMgr.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI;
    }

    public static boolean isMobileConnected(Context c) {
        ConnectivityManager connMgr = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connMgr.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_MOBILE;
    }

}
