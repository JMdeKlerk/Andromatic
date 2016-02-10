package nz.johannes.wifiler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BatteryReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String type = "";
        switch (intent.getAction()) {
            case "android.intent.action.ACTION_POWER_CONNECTED":
                type = "Charger inserted";
                break;
            case "android.intent.action.ACTION_POWER_DISCONNECTED":
                type = "Charger removed";
                break;
            case "android.intent.action.BATTERY_LOW":
                type = "Battery low";
                break;
        }
        for (Profile profile : Main.getAllStoredProfiles(context)) {
            for (Trigger trigger : profile.getTriggers()) {
                if (trigger.getType().equals(type)) profile.enable(context);
            }
        }
    }

}
