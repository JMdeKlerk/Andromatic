package nz.johannes.wifiler;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

public class BluetoothReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        BluetoothDevice bluetoothDevice;
        String deviceName;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        for (String key : prefs.getAll().keySet()) {
            if (key.startsWith("profile-")) {
                Profile profile = new Gson().fromJson(prefs.getString(key, ""), Profile.class);
                if (intent.getAction().equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
                    bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    deviceName = bluetoothDevice.getName();
                    for (Trigger trigger : profile.getTriggers()) {
                        if (trigger.getType().equals("Wifi connected") && trigger.getMatch().equals(deviceName))
                            profile.enable(context);
                    }
                }
                if (intent.getAction().equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
                    bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    deviceName = bluetoothDevice.getName();
                    for (Trigger trigger : profile.getTriggers()) {
                        if (trigger.getType().equals("Wifi disconnected") && trigger.getMatch().equals(deviceName))
                            profile.enable(context);
                    }
                    break;
                }
            }
        }
    }

}
