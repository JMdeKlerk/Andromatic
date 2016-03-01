package nz.johannes.andromatic;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Task {

    private String name;
    private static Lock taskLock = new ReentrantLock();
    private ArrayList<Trigger> triggers;
    private ArrayList<Action> actions;

    public Task() {

    }

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
        ReceiverManager.manageReceivers(context);
    }

    public void removeTrigger(Context context, Trigger trigger) {
        triggers.remove(trigger);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        String storeTask = new Gson().toJson(this);
        editor.putString("task-" + name, storeTask).apply();
        ReceiverManager.manageReceivers(context);
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

    public class TaskListViewAdapter extends ArrayAdapter<Task> {

        public TaskListViewAdapter(Context context, int resource, List<Task> items) {
            super(context, resource, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                convertView = vi.inflate(R.layout.task_row, null);
            }
            Task task = getItem(position);
            if (task != null) {
                TextView taskName = (TextView) convertView.findViewById(R.id.name);
                TextView taskDetails = (TextView) convertView.findViewById(R.id.details);
                if (taskName != null) taskName.setText(task.getName());
                if (taskDetails != null) {
                    int triggerCount = task.getTriggers().size();
                    int actionCount = task.getActions().size();
                    String triggerNeedsAnS = (triggerCount == 1) ? " trigger, " : " triggers, ";
                    String actionNeedsAnS = (actionCount == 1) ? " action)" : " actions)";
                    taskDetails.setText("(" + triggerCount + triggerNeedsAnS + actionCount + actionNeedsAnS);
                }
            }
            return convertView;
        }
    }

}
