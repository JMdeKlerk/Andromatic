package nz.johannes.wifiler;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.NetworkInfo;

import java.util.ArrayList;

public class Action {

    private String command;
    private String data;
    private ArrayList<String> multiData;
    private NetworkInfo.State requiredState;

    public Action(String command, NetworkInfo.State state) {
        this.command = command;
        this.requiredState = state;
    }

    public Action(String command, String data, NetworkInfo.State state) {
        this.command = command;
        this.data = data;
        this.requiredState = state;
    }

    public Action(String command, ArrayList<String> data, NetworkInfo.State state) {
        this.command = command;
        this.multiData = data;
        this.requiredState = state;
    }

    public void doAction(Context context, NetworkInfo.State state) {
        if (!state.equals(requiredState)) return;
        switch (getCommand()) {
            case "LAUNCH_APP":
                break;
            case "KILL_APP":
                break;
            case "SET_WIFI":
                break;
            case "SET_BLUETOOTH":
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                if (getData().equals("enabled")) adapter.enable();
                if (getData().equals("disabled")) adapter.disable();
                break;
            case "SET_MOBILE_DATA":
                break;
            case "SET_GPS":
                break;
            case "SET_RINGER_VOLUME":
                break;
            case "SET_VIBRATION":
                break;
            case "SET_BRIGHTNESS":
                break;
            case "SET_LOCK_MODE":
                break;
            case "SEND_MESSAGE":
                break;
            case "SEND_EMAIL":
                break;
            case "SET_MEDIA_VOLUME":
                break;
            case "SET_ALARM":
                break;
            case "CANCEL_ALARM":
                break;
            case "SET_CALENDAR_EVENT":
                break;
            case "CANCEL_CALENDAR_EVENT":
                break;
            case "START_TIMER":
                break;
            case "STOP_TIMER":
                break;
            case "PLAY_SOUND":
                break;
        }
    }

    public String getCommand() {
        return this.command;
    }

    public String getData() {
        return this.data;
    }

    public ArrayList getMultiData() {
        return this.multiData;
    }

    public NetworkInfo.State getRequiredState() {
        return this.requiredState;
    }

}
