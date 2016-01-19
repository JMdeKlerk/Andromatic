package nz.johannes.wifiler;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;

public class Profile {

    private String name;
    private String ssid;
    private ArrayList<Action> onConnectActions, onDisconnectActions;

    public Profile(String name, String ssid) {
        this.name = name;
        this.ssid = ssid;
        onConnectActions = new ArrayList<>();
        onDisconnectActions = new ArrayList<>();
    }

    public void doActions(Context context, NetworkInfo.State state) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        switch (state) {
            case CONNECTED:
                Main.showToast(context, "Activating profile: " + this.getName());
                editor.putString("active", this.name).commit();
                /*for (Action action : onConnectActions) {

                }*/
                break;
            case DISCONNECTED:
                Main.showToast(context, "Deactivating profile: " + this.getName());
                if (this.name.equals(prefs.getString("active", ""))) editor.remove("active").commit();
                /*for (Action action : onDisconnectActions) {

                }*/
                break;
        }
    }

    public boolean isActiveProfile(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return this.name.equals(prefs.getString("active", ""));
    }

    public String getName() {
        return this.name;
    }

    public String getSSID() {
        return this.ssid;
    }

}
