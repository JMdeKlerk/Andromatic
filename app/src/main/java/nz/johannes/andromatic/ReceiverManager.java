package nz.johannes.andromatic;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.telephony.SmsMessage;

public class ReceiverManager extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            manageReceivers(context);
        }
        if (intent.getAction().equals("android.intent.action.ACTION_SHUTDOWN")) {
            unregisterAll(context);
        }
    }

    public static void manageReceivers(Context context) {
        unregisterAll(context);
        for (Task task : Main.getAllStoredTasks(context)) {
            for (Trigger trigger : task.getTriggers()) {
                switch (trigger.getType()) {
                    case "Battery low":
                    case "Charger inserted":
                    case "Charger removed":
                        IntentFilter batteryFilter = new IntentFilter();
                        batteryFilter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
                        batteryFilter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
                        batteryFilter.addAction("android.intent.action.BATTERY_LOW");
                        context.registerReceiver(batteryReceiver, batteryFilter);
                        break;
                    case "Bluetooth connected":
                    case "Bluetooth disconnected":
                        IntentFilter bluetoothFilter = new IntentFilter();
                        bluetoothFilter.addAction("android.bluetooth.device.action.ACL_CONNECTED");
                        bluetoothFilter.addAction("android.bluetooth.device.action.ACL_DISCONNECTED");
                        context.registerReceiver(blueToothReceiver, bluetoothFilter);
                        break;
                    case "Headphones inserted":
                    case "Headphones removed":
                        IntentFilter headphoneFilter = new IntentFilter();
                        headphoneFilter.addAction("android.intent.action.HEADSET_PLUG");
                        context.registerReceiver(headphoneReceiver, headphoneFilter);
                        break;
                    case "SMS received":
                        IntentFilter smsFilter = new IntentFilter();
                        smsFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
                        context.registerReceiver(smsReceiver, smsFilter);
                        break;
                    case "Wifi connected":
                    case "Wifi disconnected":
                        IntentFilter wifiFilter = new IntentFilter();
                        wifiFilter.addAction("android.net.wifi.STATE_CHANGE");
                        context.registerReceiver(wifiReceiver, wifiFilter);
                        break;
                }
            }
        }
    }

    private static void unregisterAll(Context context) {
        BroadcastReceiver[] receivers = new BroadcastReceiver[]{batteryReceiver, blueToothReceiver, headphoneReceiver, smsReceiver, wifiReceiver};
        for (BroadcastReceiver receiver : receivers) {
            try {
                context.unregisterReceiver(receiver);
            } catch (IllegalArgumentException ex) {

            }
        }
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
            if (isInitialStickyBroadcast()) return;
            String action = (intent.getIntExtra("state", -1) == 1) ? "Headphones inserted" : "Headphones removed";
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
            SmsMessage[] messages;
            if (Build.VERSION.SDK_INT >= 19) messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            else {
                Bundle bundle = intent.getExtras();
                Object[] pdus = (Object[]) bundle.get("pdus");
                messages = new SmsMessage[pdus.length];
                for (int i = 0; i < messages.length; i++) messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
            }
            for (Task task : Main.getAllStoredTasks(context)) {
                for (Trigger trigger : task.getTriggers()) {
                    if (trigger.getType().equals("SMS received")) {
                        for (SmsMessage message : messages) {
                            String body = message.getMessageBody();
                            if (trigger.getExtraData().get(0).equals("Exact") && body.equals(trigger.getMatch()))
                                task.runTask(context);
                            if (trigger.getExtraData().get(0).equals("Partial") && body.contains(trigger.getMatch()))
                                task.runTask(context);
                        }
                    }
                }
            }
        }
    };

    private static BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
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
    };

}
