package nz.johannes.andromatic;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;

public class ReceiverManager extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

    }

    public static void registerBatteryReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
        filter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
        filter.addAction("android.intent.action.BATTERY_LOW");
        context.registerReceiver(batteryReceiver, filter);
    }

    public static void unregisterBatteryReceiver(Context context) {
        context.unregisterReceiver(batteryReceiver);
    }

    public static void registerBluetoothReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.bluetooth.device.action.ACL_CONNECTED");
        filter.addAction("android.bluetooth.device.action.ACL_DISCONNECTED");
        context.registerReceiver(blueToothReceiver, filter);
    }

    public static void unregisterBluetoothReceiver(Context context) {
        context.unregisterReceiver(blueToothReceiver);
    }

    public static void registerHeadphoneReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.HEADSET_PLUG");
        context.registerReceiver(headphoneReceiver, filter);
    }

    public static void unregisterHeadphoneReceiver(Context context) {
        context.unregisterReceiver(headphoneReceiver);
    }

    public static void registerSmsReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        context.registerReceiver(smsReceiver, filter);
    }

    public static void unregisterSmsReceiver(Context context) {
        context.unregisterReceiver(smsReceiver);
    }

    public static void registerWifiReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.wifi.STATE_CHANGE");
        context.registerReceiver(wifiReceiver, filter);
    }

    public static void unregisterWifiReceiver(Context context) {
        context.unregisterReceiver(wifiReceiver);
    }

    private static BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
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
            for (Task task : Main.getAllStoredTasks(context)) {
                for (Trigger trigger : task.getTriggers()) {
                    if (trigger.getType().equals(type)) task.runTask(context);
                }
            }
        }
    };

    private static BroadcastReceiver blueToothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String deviceName = bluetoothDevice.getName();
            String connType = (intent.getAction().equals(BluetoothDevice.ACTION_ACL_CONNECTED)) ?
                    "Bluetooth connected" : "Bluetooth disconnected";
            for (Task task : Main.getAllStoredTasks(context)) {
                for (Trigger trigger : task.getTriggers()) {
                    if (trigger.getType().equals(connType) && trigger.getMatch().equals(deviceName)) task.runTask(context);
                }
            }
        }
    };

    private static BroadcastReceiver headphoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) ?
                    "Headphones inserted" : "Headphones removed";
            for (Task task : Main.getAllStoredTasks(context)) {
                for (Trigger trigger : task.getTriggers()) {
                    if (trigger.getType().equals(action)) task.runTask(context);
                }
            }
        }
    };

    private static BroadcastReceiver smsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            for (Task task : Main.getAllStoredTasks(context)) {
                for (Trigger trigger : task.getTriggers()) {
                    if (trigger.getType().equals("SMS received")) {
                        Bundle bundle = intent.getExtras();
                        Object[] pdus = (Object[]) bundle.get("pdus");
                        SmsMessage[] messages = new SmsMessage[pdus.length];
                        for (int i = 0; i < messages.length; i++) {
                            messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                            String body = messages[i].getMessageBody();
                            if (trigger.getExtraData().get(0).equals("Exact") && body.equals(trigger.getMatch())) {
                                task.runTask(context);
                            }
                            if (trigger.getExtraData().get(0).equals("Partial") && body.contains(trigger.getMatch())) {
                                task.runTask(context);
                            }
                        }
                    }
                }
            }
        }
    };

    private static BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String ssid = "";
            if (!intent.hasExtra("bssid")) return;
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
    };

}
