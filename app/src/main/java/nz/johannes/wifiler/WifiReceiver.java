package nz.johannes.wifiler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;

public class WifiReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case ("android.net.wifi.WIFI_STATE_CHANGED"):
                String message;
                int wifiState = intent.getIntExtra("wifi_state", WifiManager.WIFI_STATE_UNKNOWN);
                if (wifiState != WifiManager.WIFI_STATE_ENABLED && wifiState != WifiManager.WIFI_STATE_DISABLED) return;
                if (wifiState == WifiManager.WIFI_STATE_ENABLED) message = "WiFi enabled - WiFiler is now active.";
                else message = "WiFi disabled - WiFiler is now stopped.";
                Main.showToast(context, message);
                break;
            case ("android.net.wifi.STATE_CHANGE"):
                if (!intent.hasExtra("bssid")) return;
                Gson gson = new Gson();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = prefs.edit();
                WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                NetworkInfo netInfo = (NetworkInfo) intent.getExtras().get("networkInfo");
                NetworkInfo.State currentState = netInfo.getState();
                String ssid = prefs.getString("lastConnectedSSID", "");
                if (currentState == NetworkInfo.State.CONNECTED) {
                    ssid = manager.getConnectionInfo().getSSID().replace("\"", "");
                    editor.putString("lastConnectedSSID", ssid).apply();
                }
                for (String key : prefs.getAll().keySet()) {
                    if (key.startsWith("profile-")) {
                        Profile profile = gson.fromJson(prefs.getString(key, ""), Profile.class);
                        if (profile.getSSID().equals(ssid)) {
                            profile.doActions(context, currentState);
                        }
                    }
                }
                break;
        }
    }

}
