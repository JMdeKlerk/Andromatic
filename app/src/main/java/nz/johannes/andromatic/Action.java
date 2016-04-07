package nz.johannes.andromatic;

import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class Action {

    private String command;
    private int data = -1;
    private ArrayList<String> multiData;

    public void doAction(Context context) {
        switch (command) {
            case "Action.StartCall":
                // TODO
                break;
            case "Action.AcceptCall":
                // TODO
                break;
            case "Action.EndCall":
                // TODO
                break;
            case "Action.SpeakerphoneEnable":
                // TODO
                break;
            case "Action.SpeakerphoneToggle":
                // TODO
                break;
            case "Action.SpeakerphoneDisable":
                // TODO
                break;
            case "Action.MicEnable":
                // TODO
                break;
            case "Action.MicToggle":
                // TODO
                break;
            case "Action.MicDisable":
                // TODO
                break;
            case "Action.SendSMS":
                SmsManager.getDefault().sendTextMessage(multiData.get(1), null, multiData.get(2), null, null);
                break;
            case "Action.MediaPlay":
                // TODO
                break;
            case "Action.MediaPause":
                // TODO
                break;
            case "Action.MediaSkip":
                // TODO
                break;
            case "Action.MediaVolume":
                AudioManager musicManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                float mediaVolume = ((float) musicManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) / 100 * data;
                musicManager.setStreamVolume(AudioManager.STREAM_MUSIC, Math.round(mediaVolume), 0);
                break;
            case "Action.LaunchApp":
                ComponentName component = new ComponentName(multiData.get(1), multiData.get(2));
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                intent.setComponent(component);
                context.startActivity(intent);
                break;
            case "Action.Vibrate":
                // TODO
                break;
            case "Action.PlaySound":
                // TODO
                break;
            case "Action.FlashLed":
                // TODO
                break;
            case "Action.BluetoothEnable":
                BluetoothAdapter.getDefaultAdapter().enable();
                break;
            case "Action.BluetoothToggle":
                // TODO
                break;
            case "Action.BluetoothDisable":
                BluetoothAdapter.getDefaultAdapter().disable();
                break;
            case "Action.MobileDataEnable":
                // TODO
                break;
            case "Action.MobileDataToggle":
                // TODO
                break;
            case "Action.MobileDataDisable":
                // TODO
                break;
            case "Action.WifiEnable":
                ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).setWifiEnabled(true);
                break;
            case "Action.WifiToggle":
                // TODO
                break;
            case "Action.WifiDisable":
                ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).setWifiEnabled(false);
                break;
            case "Action.LockModeNone":
                DevicePolicyManager lockNoneManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                ComponentName lockNoneAdmin = new ComponentName(context, DeviceAdmin.class);
                if (!lockNoneManager.isAdminActive(lockNoneAdmin)) return;
                lockNoneManager.setPasswordMinimumLength(lockNoneAdmin, 0);
                lockNoneManager.resetPassword("", DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
                break;
            case "Action.LockModePIN":
                DevicePolicyManager lockPinManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                ComponentName lockPinAdmin = new ComponentName(context, DeviceAdmin.class);
                if (!lockPinManager.isAdminActive(lockPinAdmin)) return;
                lockPinManager.setPasswordQuality(lockPinAdmin, DevicePolicyManager.PASSWORD_QUALITY_NUMERIC);
                lockPinManager.resetPassword(multiData.get(0), DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
                lockPinManager.lockNow();
                break;
            case "Action.LockModePassword":
                DevicePolicyManager lockPassManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                ComponentName lockPassAdmin = new ComponentName(context, DeviceAdmin.class);
                if (!lockPassManager.isAdminActive(lockPassAdmin)) return;
                lockPassManager.setPasswordQuality(lockPassAdmin, DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED);
                lockPassManager.resetPassword(multiData.get(0), DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
                lockPassManager.lockNow();
                break;
            case "Action.Timeout":
                // TODO
                break;
            case "Action.RingerVolume":
                AudioManager ringerManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                float ringerVolume = ((float) ringerManager.getStreamMaxVolume(AudioManager.STREAM_RING)) / 100 * data;
                ringerManager.setStreamVolume(AudioManager.STREAM_RING, Math.round(ringerVolume), 0);
                break;
            case "Action.NotificationVolume":
                AudioManager notifyManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                float notifyVolume = ((float) notifyManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)) / 100 * data;
                notifyManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, Math.round(notifyVolume), 0);
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

    public class ActionListViewAdapter extends ArrayAdapter<Action> {

        public ActionListViewAdapter(Context context, int resource, List<Action> items) {
            super(context, resource, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Action action = getItem(position);
            boolean hasExtraData = (action.getData() != -1 || action.getMultiData() != null);
            if (convertView == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                convertView = hasExtraData ? vi.inflate(R.layout.default_list_row, null) : vi.inflate(R.layout.list_row_nodetail, null);
            }
            if (action != null) {
                TextView type = (TextView) convertView.findViewById(R.id.type);
                TextView detail = (TextView) convertView.findViewById(R.id.detail);
                // TODO make human readable with per-command details
                type.setText(action.getCommand());
                if (action.getData() != -1) detail.setText(action.getData());
            }
            return convertView;
        }
    }

}
