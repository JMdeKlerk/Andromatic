package nz.johannes.andromatic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;

public class WifiReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (isInitialStickyBroadcast()) return;
        if (!intent.hasExtra("bssid")) return;
        String ssid = "";
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        NetworkInfo netInfo = (NetworkInfo) intent.getExtras().get("networkInfo");
        NetworkInfo.State currentState = netInfo.getState();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (currentState.equals(NetworkInfo.State.CONNECTED)) {
            ssid = manager.getConnectionInfo().getSSID().replace("\"", "");
            prefs.edit().putString("lastConnectedSSID", ssid).commit();
        }
        if (currentState.equals(NetworkInfo.State.DISCONNECTED)) {
            ssid = prefs.getString("lastConnectedSSID", "");
        }
        String connType = (currentState.equals(NetworkInfo.State.CONNECTED)) ? "Wifi connected" : "Wifi disconnected";
        for (Task task : Main.getAllStoredTasks(context)) {
            for (Trigger trigger : task.getTriggers()) {
                if (trigger.getType().equals(connType) && trigger.getMatch().equals(ssid)) task.runTask(context);
            }
        }
    }

}
