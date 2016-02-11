package nz.johannes.andromatic;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Task {

    private String name;
    private static Lock taskLock = new ReentrantLock();
    private ArrayList<Trigger> triggers;
    private ArrayList<Action> actions;

    public Task(Context context, String name) {
        this.name = name;
        triggers = new ArrayList<>();
        actions = new ArrayList<>();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        String storeTask = new Gson().toJson(this);
        editor.putString("task-" + name, storeTask).apply();
    }

    public void runTask(Context context) {
        taskLock.lock();
        Main.showToast(context, "Running task: " + this.getName());
        for (Action action : actions) {
            action.doAction(context);
        }
        taskLock.unlock();
    }

    public void addNewTrigger(Context context, Trigger trigger) {
        triggers.add(trigger);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        String storeTask = new Gson().toJson(this);
        editor.putString("task-" + name, storeTask).apply();
    }

    public void removeTrigger(Context context, Trigger trigger) {
        triggers.remove(trigger);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        String storeTask = new Gson().toJson(this);
        editor.putString("task-" + name, storeTask).apply();
    }

    public void addNewAction(Context context, Action action) {
        actions.add(action);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        String storeTask = new Gson().toJson(this);
        editor.putString("task-" + name, storeTask).apply();
    }

    public void removeAction(Context context, Action action) {
        actions.remove(action);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        String storeTask = new Gson().toJson(this);
        editor.putString("task-" + name, storeTask).apply();
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
