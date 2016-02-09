package nz.johannes.wifiler;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Profile {

    private String name;
    private static Lock profileLock = new ReentrantLock();
    private ArrayList<Trigger> triggers;
    private ArrayList<Action> actions;

    public Profile(Context context, String name) {
        this.name = name;
        triggers = new ArrayList<>();
        actions = new ArrayList<>();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        String storeProfile = new Gson().toJson(this);
        editor.putString("profile-" + name, storeProfile).apply();
    }

    public void toggleProfile(Context context, NetworkInfo.State state) {
        profileLock.lock();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        switch (state) {
            case CONNECTED:
                if (!this.isActiveProfile(context)) {
                    Main.showToast(context, "Activating profile: " + this.getName());
                    editor.putString("active", this.name).commit();
                }
                break;
            case DISCONNECTED:
                if (this.isActiveProfile(context)) {
                    Main.showToast(context, "Deactivating profile: " + this.getName());
                    editor.remove("active").commit();
                }
                break;
        }
        for (Action action : actions) {
            action.doAction(context, state);
        }
        profileLock.unlock();
    }

    public void addNewTrigger(Context context, Trigger trigger) {
        triggers.add(trigger);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        String storeProfile = new Gson().toJson(this);
        editor.putString("profile-" + name, storeProfile).apply();
    }

    public void removeTrigger(Context context, Trigger trigger) {
        triggers.remove(trigger);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        String storeProfile = new Gson().toJson(this);
        editor.putString("profile-" + name, storeProfile).apply();
    }

    public void addNewAction(Context context, Action action) {
        actions.add(action);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        String storeProfile = new Gson().toJson(this);
        editor.putString("profile-" + name, storeProfile).apply();
    }

    public void removeAction(Context context, Action action) {
        actions.remove(action);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        String storeProfile = new Gson().toJson(this);
        editor.putString("profile-" + name, storeProfile).apply();
    }

    public boolean isActiveProfile(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return this.name.equals(prefs.getString("active", ""));
    }

    public ArrayList<Trigger> getTriggers() {
        return triggers;
    }

    public ArrayList<Action> getActions() {
        return actions;
    }

    public String getName() {
        return this.name;
    }

}
