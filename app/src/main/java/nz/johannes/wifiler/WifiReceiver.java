package nz.johannes.wifiler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

public class WifiReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.net.wifi.STATE_CHANGE")) {
            if (!intent.hasExtra("bssid")) return;
            WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            NetworkInfo netInfo = (NetworkInfo) intent.getExtras().get("networkInfo");
            NetworkInfo.State currentState = netInfo.getState();
            String ssid = manager.getConnectionInfo().getSSID().replace("\"", "");
            String connType = (currentState.equals(NetworkInfo.State.CONNECTED)) ? "Wifi connected" : "Wifi disconnected";
            for (Profile profile : Main.getAllStoredProfiles(context)) {
                for (Trigger trigger : profile.getTriggers()) {
                    if (trigger.getType().equals(connType) && trigger.getMatch().equals(ssid)) profile.enable(context);
                }
            }
        }
    }

}
