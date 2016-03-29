package nz.johannes.andromatic;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class TaskEditor extends AppCompatActivity {

    private Task task;
    private View view;
    private AutoCompleteTextView textView;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taskedit);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String taskName = getIntent().getStringExtra("task");
        task = new Gson().fromJson(prefs.getString("task-" + taskName, ""), Task.class);
        setTitle(task.getName());
        activity = this;
        populateTriggerList();
        populateConditionsList();
        populateActionsList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            for (Task task : Main.getAllStoredTasks(this)) {
                for (Action action : task.getActions()) {
                    if (action.getCommand().equals("Set lock mode")) task.removeAction(this, action);
                }
            }
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                recreate();
            }
        }, 0);
    }

    private void populateTriggerList() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final ListView triggerList = (ListView) findViewById(R.id.triggerList);
        final ArrayList<Trigger> listItems = new ArrayList<>();
        final Trigger.TriggerListViewAdapter adapter = new Trigger().new TriggerListViewAdapter(this, R.layout.default_list_row, listItems);
        registerForContextMenu(triggerList);
        triggerList.setAdapter(adapter);
        triggerList.setClickable(true);
        triggerList.setLongClickable(true);
        triggerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Trigger trigger = (Trigger) triggerList.getItemAtPosition(position);
                if (trigger.getType().equals("Add new...")) {
                    addTrigger();
                }
            }
        });
        triggerList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Trigger triggerToDelete = (Trigger) triggerList.getItemAtPosition(position);
                if (triggerToDelete.getType().equals("Add new...")) {
                    addTrigger();
                    return true;
                }
                alert.setTitle("Delete trigger?");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        task.removeTrigger(getBaseContext(), triggerToDelete);
                        populateTriggerList();
                    }
                });
                alert.setNegativeButton("No", null);
                alert.show();
                return true;
            }
        });
        for (Trigger trigger : task.getTriggers()) listItems.add(trigger);
        listItems.add(new Trigger("Add new..."));
        adapter.notifyDataSetChanged();
        setDynamicListHeight(triggerList);
    }

    private void populateConditionsList() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final ListView conditionList = (ListView) findViewById(R.id.conditionList);
        final ArrayList<Condition> listItems = new ArrayList<>();
        final Condition.ConditionListViewAdapter adapter = new Condition().new ConditionListViewAdapter(this, R.layout.default_list_row, listItems);
        registerForContextMenu(conditionList);
        conditionList.setAdapter(adapter);
        conditionList.setClickable(true);
        conditionList.setLongClickable(true);
        conditionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Condition condition = (Condition) conditionList.getItemAtPosition(position);
                if (condition.getType().equals("Add new...")) {
                    addCondition();
                }
            }
        });
        conditionList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Condition conditionToDelete = (Condition) conditionList.getItemAtPosition(position);
                if (conditionToDelete.getType().equals("Add new...")) {
                    addCondition();
                    return true;
                }
                alert.setTitle("Delete trigger?");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        task.removeCondition(getBaseContext(), conditionToDelete);
                        populateConditionsList();
                    }
                });
                alert.setNegativeButton("No", null);
                alert.show();
                return true;
            }
        });
        for (Condition condition : task.getConditions()) listItems.add(condition);
        listItems.add(new Condition("Add new..."));
        adapter.notifyDataSetChanged();
        setDynamicListHeight(conditionList);
    }

    private void populateActionsList() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final ListView actionList = (ListView) findViewById(R.id.actionList);
        final ArrayList<Action> listItems = new ArrayList<>();
        final Action.ActionListViewAdapter adapter = new Action().new ActionListViewAdapter(this, R.layout.default_list_row, listItems);
        registerForContextMenu(actionList);
        actionList.setAdapter(adapter);
        actionList.setClickable(true);
        actionList.setLongClickable(true);
        actionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Action action = (Action) actionList.getItemAtPosition(position);
                if (action.getCommand().equals("Add new...")) {
                    addAction();
                }
            }
        });
        actionList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Action actionToDelete = (Action) actionList.getItemAtPosition(position);
                if (actionToDelete.getCommand().equals("Add new...")) {
                    addAction();
                    return true;
                }
                alert.setTitle("Delete action?");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        task.removeAction(getBaseContext(), actionToDelete);
                        populateActionsList();
                    }
                });
                alert.setNegativeButton("No", null);
                alert.show();
                return true;
            }
        });
        for (Action action : task.getActions()) listItems.add(action);
        Action addNew = new Action();
        addNew.setCommand("Add new...");
        listItems.add(addNew);
        adapter.notifyDataSetChanged();
        setDynamicListHeight(actionList);
    }

    public void addTrigger() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final String[] triggerChoices = new String[]{"Battery low", "Bluetooth connected", "Bluetooth disconnected", "Charger inserted",
                "Charger removed", "Interval", "SMS received", "Time", "Wifi connected", "Wifi disconnected"};
        alert.setItems(triggerChoices, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final Trigger trigger = new Trigger(triggerChoices[which]);
                switch (triggerChoices[which]) {
                    case "Bluetooth connected":
                    case "Bluetooth disconnected":
                        alert.setItems(null, null);
                        view = getLayoutInflater().inflate(R.layout.dialog_autocomplete, null);
                        textView = (AutoCompleteTextView) view.findViewById(R.id.text);
                        textView.setHint("Device name");
                        textView.setAdapter(Main.getTextViewAdapter(getBaseContext(), "bluetooth"));
                        alert.setView(view);
                        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AutoCompleteTextView deviceNameField = (AutoCompleteTextView) ((AlertDialog) dialog).findViewById(R.id.text);
                                String device = deviceNameField.getText().toString();
                                trigger.setMatch(device);
                                task.addNewTrigger(getBaseContext(), trigger);
                                populateTriggerList();
                            }
                        });
                        alert.setNegativeButton("Cancel", null);
                        alert.show();
                        break;
                    case "SMS received":
                        final String[] typeChoices = new String[]{"Content", "Sender"};
                        alert.setItems(typeChoices, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (typeChoices[which]) {
                                    case "Content":
                                        alert.setItems(null, null);
                                        alert.setView(R.layout.dialog_incomingmessage);
                                        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                EditText matchText = (EditText) ((AlertDialog) dialog).findViewById(R.id.content);
                                                RadioButton exact = (RadioButton) ((AlertDialog) dialog).findViewById(R.id.radio_exact);
                                                String match = matchText.getText().toString();
                                                trigger.setMatch(match);
                                                ArrayList<String> extras = new ArrayList<>();
                                                extras.add("Content");
                                                extras.add(exact.isChecked() ? "Exact" : "Partial");
                                                trigger.setExtraData(extras);
                                                task.addNewTrigger(getBaseContext(), trigger);
                                                populateTriggerList();
                                            }
                                        });
                                        alert.setNegativeButton("Cancel", null);
                                        alert.show();
                                        break;
                                    case "Sender":
                                        alert.setItems(null, null);
                                        view = getLayoutInflater().inflate(R.layout.dialog_autocomplete, null);
                                        textView = (AutoCompleteTextView) view.findViewById(R.id.text);
                                        textView.setHint("Name/number");
                                        textView.setAdapter(Main.getTextViewAdapter(getBaseContext(), "contacts"));
                                        textView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                                                String selection = ((TextView) view).getText().toString();
                                                selection = selection.substring(selection.indexOf("(") + 1, selection.indexOf(")"));
                                                textView.setText(selection);
                                            }
                                        });
                                        alert.setView(view);
                                        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                AutoCompleteTextView senderField = (AutoCompleteTextView) ((AlertDialog) dialog).findViewById(R.id.text);
                                                String sender = senderField.getText().toString();
                                                trigger.setMatch(sender);
                                                ArrayList<String> extras = new ArrayList<>();
                                                extras.add("Sender");
                                                extras.add(Main.getNameFromNumber(getBaseContext(), sender));
                                                trigger.setExtraData(extras);
                                                task.addNewTrigger(getBaseContext(), trigger);
                                                populateTriggerList();
                                            }
                                        });
                                        alert.setNegativeButton("Cancel", null);
                                        alert.show();
                                        break;
                                }
                            }
                        });
                        alert.show();
                        break;
                    case "Interval":
                        final String timeChoices[] = new String[]{"1 minute", "5 minutes", "10 minutes", "30 minutes", "60 minutes", "120 minutes"};
                        alert.setItems(timeChoices, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                trigger.setMatch(timeChoices[which]);
                                task.addNewTrigger(getBaseContext(), trigger);
                                populateTriggerList();
                            }
                        });
                        alert.show();
                        break;
                    case "Time":
                        TimePickerDialog timePicker = new TimePickerDialog(TaskEditor.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                String meridian = "AM";
                                ArrayList<String> extras = new ArrayList<>();
                                extras.add(String.valueOf(hourOfDay));
                                extras.add(String.valueOf(minute));
                                if (hourOfDay >= 12) {
                                    hourOfDay = hourOfDay - 12;
                                    if (hourOfDay == 0) hourOfDay = 12;
                                    meridian = "PM";
                                }
                                String leadingZeroHour = (hourOfDay < 10) ? "0" : "";
                                String leadingZeroMinute = (minute < 10) ? "0" : "";
                                trigger.setMatch(leadingZeroHour + hourOfDay + ":" + leadingZeroMinute + minute + " " + meridian);
                                trigger.setExtraData(extras);
                                task.addNewTrigger(getBaseContext(), trigger);
                                populateTriggerList();
                            }
                        }, 0, 0, false);
                        timePicker.setTitle(null);
                        timePicker.show();
                        break;
                    case "Wifi connected":
                    case "Wifi disconnected":
                        alert.setItems(null, null);
                        view = getLayoutInflater().inflate(R.layout.dialog_autocomplete, null);
                        textView = (AutoCompleteTextView) view.findViewById(R.id.text);
                        textView.setHint("SSID");
                        textView.setAdapter(Main.getTextViewAdapter(getBaseContext(), "ssids"));
                        alert.setView(view);
                        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EditText ssidField = (EditText) ((AlertDialog) dialog).findViewById(R.id.text);
                                String ssid = ssidField.getText().toString();
                                trigger.setMatch(ssid);
                                task.addNewTrigger(getBaseContext(), trigger);
                                populateTriggerList();
                            }
                        });
                        alert.setNegativeButton("Cancel", null);
                        alert.show();
                        break;
                    default:
                        task.addNewTrigger(getBaseContext(), trigger);
                        populateTriggerList();
                        break;
                }
            }
        });
        alert.show();
    }

    public void addCondition() {
        //TODO
        Condition condition = new Condition("Wifi is connected");
        task.addNewCondition(getBaseContext(), condition);
        populateConditionsList();
    }

    public void addAction() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final Action action = new Action();
        final String actionChoices[] = new String[]{"Launch app", "Enable wifi", "Disable wifi", "Enable bluetooth", "Disable bluetooth",
                "Set ringer volume", "Set notification volume", "Set media volume", "Set lock mode", "Set screen timeout", "Send SMS"};
        alert.setItems(actionChoices, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String actionChoice = actionChoices[which];
                switch (actionChoice) {
                    case "Launch app":
                        PackageManager pm = getPackageManager();
                        final ArrayList<String> appChoices = new ArrayList<>();
                        final ArrayList<String> appChoicePackage = new ArrayList<>();
                        final ArrayList<String> appChoiceName = new ArrayList<>();
                        Intent main = new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER);
                        List<ResolveInfo> packages = pm.queryIntentActivities(main, 0);
                        for (ResolveInfo appInfo : packages) {
                            appChoices.add(appInfo.loadLabel(pm).toString());
                            appChoicePackage.add(appInfo.activityInfo.packageName);
                            appChoiceName.add(appInfo.activityInfo.name);
                        }
                        alert.setItems(appChoices.toArray(new String[appChoices.size()]), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                action.setCommand(actionChoice);
                                ArrayList actionData = new ArrayList();
                                actionData.add(appChoices.get(which));
                                actionData.add(appChoicePackage.get(which));
                                actionData.add(appChoiceName.get(which));
                                action.setData(actionData);
                                task.addNewAction(getBaseContext(), action);
                                populateActionsList();
                            }
                        });
                        alert.show();
                        break;
                    case "Set ringer volume":
                    case "Set notification volume":
                    case "Set media volume":
                        alert.setItems(null, null);
                        view = getLayoutInflater().inflate(R.layout.dialog_seekbar, null);
                        ((SeekBar) view.findViewById(R.id.seek)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                TextView seekText = (TextView) view.findViewById(R.id.seek_text);
                                seekText.setText(progress * 10 + "%");
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {
                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {
                            }
                        });
                        alert.setView(view);
                        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                action.setCommand(actionChoice);
                                SeekBar seek = (SeekBar) ((AlertDialog) dialog).findViewById(R.id.seek);
                                action.setData(seek.getProgress() * 10);
                                task.addNewAction(getBaseContext(), action);
                                populateActionsList();
                            }
                        });
                        alert.setNegativeButton("Cancel", null);
                        alert.show();
                        break;
                    case "Send SMS":
                        view = getLayoutInflater().inflate(R.layout.dialog_sendmessage, null);
                        textView = (AutoCompleteTextView) view.findViewById(R.id.to);
                        textView.setHint("Test");
                        textView.setAdapter(Main.getTextViewAdapter(getBaseContext(), "contacts"));
                        textView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                                String selection = ((TextView) view).getText().toString();
                                selection = selection.substring(selection.indexOf("(") + 1, selection.indexOf(")"));
                                textView.setText(selection);
                            }
                        });
                        alert.setItems(null, null);
                        alert.setView(view);
                        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                action.setCommand(actionChoice);
                                EditText to = (EditText) ((AlertDialog) dialog).findViewById(R.id.to);
                                EditText message = (EditText) ((AlertDialog) dialog).findViewById(R.id.content);
                                ArrayList actionData = new ArrayList();
                                actionData.add(Main.getNameFromNumber(getBaseContext(), to.getText().toString()));
                                actionData.add(to.getText().toString());
                                actionData.add(message.getText().toString());
                                action.setData(actionData);
                                task.addNewAction(getBaseContext(), action);
                                populateActionsList();
                            }
                        });
                        alert.setNegativeButton("Cancel", null);
                        alert.show();
                        break;
                    case "Set lock mode":
                        if (!Main.checkOrRequestDeviceAdmin(getBaseContext(), activity)) return;
                        final String lockChoices[] = new String[]{"None", "PIN", "Password"};
                        alert.setItems(lockChoices, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                action.setCommand(actionChoice);
                                action.setData(which);
                                if (which == 0) {
                                    task.addNewAction(getBaseContext(), action);
                                    populateActionsList();
                                } else {
                                    alert.setItems(null, null);
                                    view = getLayoutInflater().inflate(R.layout.dialog_singleline, null);
                                    EditText textView = (EditText) view.findViewById(R.id.text);
                                    textView.setHint((which == 1) ? "PIN" : "Password");
                                    textView.setInputType((which == 1) ? InputType.TYPE_CLASS_NUMBER : InputType.TYPE_CLASS_TEXT);
                                    textView.setTransformationMethod(PasswordTransformationMethod.getInstance());
                                    alert.setView(view);
                                    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            EditText password = (EditText) ((AlertDialog) dialog).findViewById(R.id.text);
                                            ArrayList data = new ArrayList();
                                            data.add(password.getText().toString());
                                            action.setData(data);
                                            task.addNewAction(getBaseContext(), action);
                                            populateActionsList();
                                        }
                                    });
                                    alert.setNegativeButton("Cancel", null);
                                    alert.show();
                                }
                            }
                        });
                        alert.show();
                        break;
                    case "Set screen timeout":
                        if (!Main.checkOrRequestDeviceAdmin(getBaseContext(), activity)) return;
                        final String timeoutChoices[] = new String[]{"15 seconds", "30 seconds", "1 minute", "2 minutes", "5 minutes", "10 minutes"};
                        alert.setItems(timeoutChoices, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                action.setCommand(actionChoice);
                                action.setData(which);
                                task.addNewAction(getBaseContext(), action);
                                populateActionsList();
                            }
                        });
                        alert.show();
                        break;
                    default:
                        action.setCommand(actionChoice);
                        task.addNewAction(getBaseContext(), action);
                        populateActionsList();
                        break;
                }
            }
        });
        alert.show();
    }

    private static void setDynamicListHeight(ListView mListView) {
        ListAdapter mListAdapter = mListView.getAdapter();
        if (mListAdapter == null) return;
        int height = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(mListView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        for (int i = 0; i < mListAdapter.getCount(); i++) {
            View listItem = mListAdapter.getView(i, null, mListView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            height += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = mListView.getLayoutParams();
        params.height = height + (mListView.getDividerHeight() * (mListAdapter.getCount() - 1));
        mListView.setLayoutParams(params);
        mListView.requestLayout();
    }

}
