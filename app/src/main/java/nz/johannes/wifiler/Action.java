package nz.johannes.wifiler;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
        switch (command) {
            case "Launch app":
                // Works
                ComponentName component = new ComponentName(multiData.get(1), multiData.get(2));
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                intent.setComponent(component);
                context.startActivity(intent);
                break;
            case "Kill app":
                // Works
                ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                activityManager.killBackgroundProcesses(multiData.get(1));
                break;
            case "Enable wifi":
                // Works
                ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).setWifiEnabled(true);
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
            case "Set brightness":
                //TODO
                break;
            case "Set ringer volume":
                // Works
                AudioManager ringerManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                float ringerVolume = ((float) ringerManager.getStreamMaxVolume(AudioManager.STREAM_RING)) / 100 * data;
                ringerManager.setStreamVolume(AudioManager.STREAM_RING, Math.round(ringerVolume), 0);
                break;
            case "Set notification volume":
                // Works
                AudioManager notifyManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                float notifyVolume = ((float) notifyManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)) / 100 * data;
                notifyManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, Math.round(notifyVolume), 0);
                break;
            case "Set media volume":
                // Works
                AudioManager musicManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                float mediaVolume = ((float) musicManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) / 100 * data;
                musicManager.setStreamVolume(AudioManager.STREAM_MUSIC, Math.round(mediaVolume), 0);
                break;
            case "Set lock mode":
                //TODO
                break;
            case "Send SMS":
                // Works
                SmsManager.getDefault().sendTextMessage((String) multiData.get(1), null, (String) multiData.get(2), null, null);
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
