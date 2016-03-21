package nz.johannes.andromatic;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Task {

    private String name;
    private static Lock taskLock = new ReentrantLock();
    private ArrayList<Trigger> triggers;
    private ArrayList<Condition> conditions;
    private ArrayList<Action> actions;
    private ArrayList<Action> actionsWaitingForDeviceAdmin;

    public Task() {

    }

    public Task(Context context, String name) {
        this.name = name;
        triggers = new ArrayList<>();
        conditions = new ArrayList<>();
        actions = new ArrayList<>();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        String storeTask = new Gson().toJson(this);
        editor.putString("task-" + name, storeTask).apply();
    }

    public void runTask(Context context) {
        taskLock.lock();
        for (Condition condition : conditions) {
            if (!condition.check()) return;
        }
        Main.showToast(context, "Running task: " + this.getName());
        for (Action action : actions) {
            action.doAction(context);
        }
        taskLock.unlock();
    }

    public void addNewTrigger(Context context, Trigger trigger) {
        unsetAlarms(context);
        triggers.add(trigger);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        String storeTask = new Gson().toJson(this);
        editor.putString("task-" + name, storeTask).apply();
        setAlarms(context);
    }

    public void removeTrigger(Context context, Trigger trigger) {
        unsetAlarms(context);
        triggers.remove(trigger);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        String storeTask = new Gson().toJson(this);
        editor.putString("task-" + name, storeTask).apply();
        setAlarms(context);
    }

    public void addNewCondition(Context context, Condition condition) {
        conditions.add(condition);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        String storeTask = new Gson().toJson(this);
        editor.putString("task-" + name, storeTask).apply();
    }

    public void removeCondition(Context context, Condition condition) {
        conditions.remove(condition);
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

    public void addNewActionToWaitList(Context context, Action action) {
        actionsWaitingForDeviceAdmin.add(action);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        String storeTask = new Gson().toJson(this);
        editor.putString("task-" + name, storeTask).apply();
    }

    public void addAllWaitingActions(Context context) {
        for (Action action : actionsWaitingForDeviceAdmin) {
            this.addNewAction(context, action);
        }
        actionsWaitingForDeviceAdmin = null;
    }

    public void removeAction(Context context, Action action) {
        actions.remove(action);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        String storeTask = new Gson().toJson(this);
        editor.putString("task-" + name, storeTask).apply();
    }

    public void setAlarms(Context context) {
        Intent alarm = new Intent("nz.johannes.ALARM_INTENT");
        AlarmManager aManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        for (Trigger trigger : triggers) {
            if (trigger.getType().equals("Interval")) {
                alarm.setData(Uri.parse("task://" + name + "/" + this.triggers.indexOf(trigger)));
                PendingIntent pi = PendingIntent.getBroadcast(context, 0, alarm, 0);
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.SECOND, 0);
                switch (trigger.getMatch()) {
                    case "1 minute":
                        calendar.add(Calendar.MINUTE, 1);
                        break;
                    case "5 minutes":
                        do calendar.add(Calendar.MINUTE, 1);
                        while (calendar.get(Calendar.MINUTE) % 5 != 0);
                        break;
                    case "10 minutes":
                        do calendar.add(Calendar.MINUTE, 1);
                        while (calendar.get(Calendar.MINUTE) % 10 != 0);
                        break;
                    case "30 minutes":
                        do calendar.add(Calendar.MINUTE, 1);
                        while (calendar.get(Calendar.MINUTE) % 30 != 0);
                        break;
                    case "60 minutes":
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.add(Calendar.HOUR_OF_DAY, 1);
                        break;
                    case "120 minutes":
                        calendar.set(Calendar.MINUTE, 0);
                        do calendar.add(Calendar.HOUR_OF_DAY, 1);
                        while (calendar.get(Calendar.HOUR_OF_DAY) % 2 != 0);
                        break;
                }
                Log.i("Log", "Set for " + new Date(calendar.getTimeInMillis()) + " - done");
                if (android.os.Build.VERSION.SDK_INT >= 19) aManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
                else aManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
            }
            if (trigger.getType().equals("Time")) {
                alarm.setData(Uri.parse("task://" + name + "/" + this.triggers.indexOf(trigger)));
                PendingIntent pi = PendingIntent.getBroadcast(context, 0, alarm, 0);
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(trigger.getExtraData().get(0)));
                calendar.set(Calendar.MINUTE, Integer.parseInt(trigger.getExtraData().get(1)));
                calendar.set(Calendar.SECOND, 0);
                if (calendar.getTimeInMillis() < System.currentTimeMillis()) calendar.add(Calendar.HOUR_OF_DAY, 24);
                if (android.os.Build.VERSION.SDK_INT >= 19) aManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
                else aManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
            }
        }
    }

    public void unsetAlarms(Context context) {
        Intent alarm = new Intent("nz.johannes.ALARM_INTENT");
        AlarmManager aManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        for (Trigger trigger : triggers) {
            if (trigger.getType().equals("Interval") || trigger.getType().equals("Time")) {
                alarm.setData(Uri.parse("task://" + name + this.triggers.indexOf(trigger)));
                PendingIntent pi = PendingIntent.getBroadcast(context, 0, alarm, 0);
                aManager.cancel(pi);
            }
        }
    }

    public ArrayList<Trigger> getTriggers() {
        return triggers;
    }

    public ArrayList<Condition> getConditions() {
        return conditions;
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
