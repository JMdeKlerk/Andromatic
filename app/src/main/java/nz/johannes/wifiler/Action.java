package nz.johannes.wifiler;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.telephony.SmsManager;

import java.util.ArrayList;

public class Action {

    private String command;
    private int data = -1;
    private ArrayList<String> multiData;
    private NetworkInfo.State requiredState;

    public void doAction(Context context, NetworkInfo.State state) {
        if (!state.equals(requiredState)) return;
        Intent intent;
        AudioManager am;
        switch (command) {
            case "Launch app":
                //crash
                PackageManager pm = context.getPackageManager();
                intent = pm.getLaunchIntentForPackage(multiData.get(1));
                context.startActivity(intent);
                break;
            case "Kill app":
                //crash
                ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                activityManager.killBackgroundProcesses(multiData.get(1));
                break;
            case "Disable wifi":
                // Works
                ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).setWifiEnabled(false);
                break;
            case "Enable bluetooth":
                // Works
                BluetoothAdapter.getDefaultAdapter().enable();
                break;
            case "Disable bluetooth":
                // Works
                BluetoothAdapter.getDefaultAdapter().disable();
                break;
            case "Enable GPS":
                // Crash
                intent = new Intent("android.location.GPS_ENABLED_CHANGE");
                intent.putExtra("enabled", true);
                context.sendBroadcast(intent);
                break;
            case "Disable GPS":
                // Crash
                intent = new Intent("android.location.GPS_ENABLED_CHANGE");
                intent.putExtra("enabled", false);
                context.sendBroadcast(intent);
                break;
            case "Set brightness":
                //TODO
                break;
            case "Set ringer volume":
                am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                int ringerVolume = (am.getStreamMaxVolume(AudioManager.STREAM_RING) / 100) * data;
                am.setStreamVolume(AudioManager.STREAM_RING, ringerVolume, 0);
                break;
            case "Set notification volume":
                am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                int notifyVolume = (am.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION) / 100) * data;
                am.setStreamVolume(AudioManager.STREAM_NOTIFICATION, notifyVolume, 0);
                break;
            case "Set media volume":
                am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                int mediaVolume = (am.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 100) * data;
                am.setStreamVolume(AudioManager.STREAM_MUSIC, mediaVolume, 0);
                break;
            case "Set lock mode":
                //TODO
                break;
            case "Send SMS":
                SmsManager.getDefault().sendTextMessage(multiData.get(2), null, multiData.get(3), null, null);
                break;
            case "Send email":
                //TODO
                break;
        }
    }

    public String getCommand() {
        return this.command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public int getData() {
        return this.data;
    }

    public void setData(int data) {
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
