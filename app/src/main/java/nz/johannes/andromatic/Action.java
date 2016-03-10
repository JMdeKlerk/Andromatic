package nz.johannes.andromatic;

import android.app.ActivityManager;
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
            case "Launch app":
                ComponentName component = new ComponentName(multiData.get(1), multiData.get(2));
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                intent.setComponent(component);
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
            case "Set brightness":
                //TODO
                break;
            case "Set ringer volume":
                AudioManager ringerManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                float ringerVolume = ((float) ringerManager.getStreamMaxVolume(AudioManager.STREAM_RING)) / 100 * data;
                ringerManager.setStreamVolume(AudioManager.STREAM_RING, Math.round(ringerVolume), 0);
                break;
            case "Set notification volume":
                AudioManager notifyManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                float notifyVolume = ((float) notifyManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)) / 100 * data;
                notifyManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, Math.round(notifyVolume), 0);
                break;
            case "Set media volume":
                AudioManager musicManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                float mediaVolume = ((float) musicManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) / 100 * data;
                musicManager.setStreamVolume(AudioManager.STREAM_MUSIC, Math.round(mediaVolume), 0);
                break;
            case "Set lock mode":
                //TODO
                break;
            case "Send SMS":
                SmsManager.getDefault().sendTextMessage(multiData.get(1), null, multiData.get(2), null, null);
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
                if (type != null) {
                    type.setText(action.getCommand());
                }
                if (detail != null) {
                    if (hasExtraData) switch (action.getCommand()) {
                        case ("Launch app"):
                        case ("Kill app"):
                            detail.setText("App: " + action.getMultiData().get(0));
                            break;
                        case "Set brightness":
                        case "Set ringer volume":
                        case "Set notification volume":
                        case "Set media volume":
                            detail.setText(action.getData() + " percent");
                            break;
                        case "Send SMS":
                        case "Send email":
                            detail.setText("To: " + action.getMultiData().get(0) + " (" + action.getMultiData().get(1) + ")");
                            break;
                        case "Set lock mode":
                            String lockChoices[] = new String[]{"None", "PIN", "Gesture", "Fingerprint"};
                            detail.setText(lockChoices[action.getData()]);
                            break;
                    }
                }
            }
            return convertView;
        }
    }

}