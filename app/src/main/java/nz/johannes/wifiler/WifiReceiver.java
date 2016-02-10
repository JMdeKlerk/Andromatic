package nz.johannes.wifiler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

public class WifiReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.net.wifi.STATE_CHANGE")) {
            if (!intent.hasExtra("bssid")) return;
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
                    Profile profile = new Gson().fromJson(prefs.getString(key, ""), Profile.class);
                    if (currentState.equals(NetworkInfo.State.CONNECTED)) {
                        for (Trigger trigger : profile.getTriggers()) {
                            if (trigger.getType().equals("Wifi connected") && trigger.getMatch().equals(ssid))
                                profile.enable(context);
                        }
                    } else {
                        for (Trigger trigger : profile.getTriggers()) {
                            if (trigger.getType().equals("Wifi disconnected") && trigger.getMatch().equals(ssid))
                                profile.enable(context);
                        }
                        break;
                    }
                }
            }
        }
    }

}
