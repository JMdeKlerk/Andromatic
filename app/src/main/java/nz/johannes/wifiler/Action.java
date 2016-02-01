package nz.johannes.wifiler;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import java.util.ArrayList;

public class Action {

    private String command;
    private String data;
    private ArrayList<String> multiData;
    private NetworkInfo.State requiredState;

    private Intent intent;

    public void doAction(Context context, NetworkInfo.State state) {
        if (!state.equals(requiredState)) return;
        switch (command) {
            case "Launch app":
                PackageManager pm = context.getPackageManager();
                intent = pm.getLaunchIntentForPackage(multiData.get(1));
                context.startActivity(intent);
                break;
            case "Kill app":
                ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                activityManager.killBackgroundProcesses(multiData.get(1));
                break;
            case "Enable wifi":
                ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).setWifiEnabled(true);
                break;
            case "Disable wifi":
                ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).setWifiEnabled(false);
                break;
            case "Enable bluetooth":
                BluetoothAdapter.getDefaultAdapter().enable();
                break;
            case "Disable bluetooth":
                BluetoothAdapter.getDefaultAdapter().disable();
                break;
            case "Enable GPS":
                intent = new Intent("android.location.GPS_ENABLED_CHANGE");
                intent.putExtra("enabled", true);
                context.sendBroadcast(intent);
                break;
            case "Disable GPS":
                intent = new Intent("android.location.GPS_ENABLED_CHANGE");
                intent.putExtra("enabled", false);
                context.sendBroadcast(intent);
                break;
            case "Set brightness":
                break;
            case "Set ringer volume":
                break;
            case "Set media volume":
                break;
            case "Set lock mode":
                break;
            case "Send SMS":
                break;
            case "Send email":
                break;
            case "Start timer":
                break;
            case "Stop timer":
                break;
            case "Shut down phone":
                break;
        }
    }

    public String getCommand() {
        return this.command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getData() {
        return this.data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public ArrayList getMultiData() {
        return this.multiData;
    }

    public void setData(ArrayList data) {
        this.multiData = data;
    }

    public NetworkInfo.State getRequiredState() {
        return this.requiredState;
    }

    public void setRequiredState(NetworkInfo.State state) {
        this.requiredState = state;
    }

}
