package nz.johannes.wifiler;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BluetoothReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        String deviceName = bluetoothDevice.getName();
        String connType = (intent.getAction().equals(BluetoothDevice.ACTION_ACL_CONNECTED)) ? "Bluetooth connected" : "Bluetooth disconnected";
        for (Profile profile : Main.getAllStoredProfiles(context)) {
            for (Trigger trigger : profile.getTriggers()) {
                if (trigger.getType().equals(connType) && trigger.getMatch().equals(deviceName)) profile.enable(context);
            }
        }
    }

}
