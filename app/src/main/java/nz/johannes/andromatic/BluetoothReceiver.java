package nz.johannes.andromatic;

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
        for (Task task : Main.getAllStoredTasks(context)) {
            for (Trigger trigger : task.getTriggers()) {
                if (trigger.getType().equals(connType) && trigger.getMatch().equals(deviceName)) task.runTask(context);
            }
        }
    }

}
