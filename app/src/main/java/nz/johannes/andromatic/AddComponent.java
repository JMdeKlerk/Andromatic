package nz.johannes.andromatic;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class AddComponent extends PreferenceActivity {

    private static Task task;
    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;
        task = Main.getTask(this, getIntent().getStringExtra("Task"));
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        this.getWindow().setAttributes(params);
        String componentType = getIntent().getAction();
        switch (componentType) {
            case "TRIGGER":
                break;
            case "CONDITION":
                break;
            case "ACTION":
                getFragmentManager().beginTransaction().replace(android.R.id.content, new ActionFragment()).commit();
                break;
        }
    }

    public static class ActionFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.actions);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            final View view;
            final Action action = new Action();
            final AlertDialog.Builder alert = new AlertDialog.Builder(context);
            final String actionName = (String) preference.getTitle();
            switch (actionName) {
                case "Start call":
                    break;
                case "Send SMS message":
                    view = getActivity().getLayoutInflater().inflate(R.layout.dialog_sendmessage, null);
                    final AutoCompleteTextView textView = (AutoCompleteTextView) view.findViewById(R.id.to);
                    textView.setHint("Name/number");
                    textView.setAdapter(Main.getTextViewAdapter(context, "contacts"));
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
                            action.setCommand("Send SMS message");
                            EditText to = (EditText) ((AlertDialog) dialog).findViewById(R.id.to);
                            EditText message = (EditText) ((AlertDialog) dialog).findViewById(R.id.content);
                            ArrayList actionData = new ArrayList();
                            actionData.add(Main.getNameFromNumber(context, to.getText().toString()));
                            actionData.add(to.getText().toString());
                            actionData.add(message.getText().toString());
                            action.setData(actionData);
                            task.addNewAction(context, action);
                            getActivity().finish();
                        }
                    });
                    alert.setNegativeButton("Cancel", null);
                    alert.show();
                    break;
                case "Set media volume":
                case "Set ringer volume":
                case "Set notification volume":
                    view = getActivity().getLayoutInflater().inflate(R.layout.dialog_seekbar, null);
                    SeekBar seek = (SeekBar) view.findViewById(R.id.seek);
                    seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
                            SeekBar seek = (SeekBar) ((AlertDialog) dialog).findViewById(R.id.seek);
                            action.setData(seek.getProgress() * 10);
                            task.addNewAction(context, action);
                            getActivity().finish();
                        }
                    });
                    alert.setNegativeButton("Cancel", null);
                    alert.show();
                    break;
                case "Launch app":
                    PackageManager pm = context.getPackageManager();
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
                            action.setCommand(actionName);
                            ArrayList actionData = new ArrayList();
                            actionData.add(appChoices.get(which));
                            actionData.add(appChoicePackage.get(which));
                            actionData.add(appChoiceName.get(which));
                            action.setData(actionData);
                            task.addNewAction(context, action);
                            getActivity().finish();
                        }
                    });
                    alert.show();
                    break;
                case "Play sound":
                    break;
                case "Set lock mode":
                    if (!Main.checkOrRequestDeviceAdmin(context, getActivity())) return false;
                    view = getActivity().getLayoutInflater().inflate(R.layout.dialog_singleline, null);
                    final String lockChoices[] = new String[]{"None", "PIN", "Password"};
                    alert.setItems(lockChoices, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            action.setCommand(actionName);
                            action.setData(which);
                            if (which == 0) {
                                task.addNewAction(context, action);
                                getActivity().finish();
                            } else {
                                alert.setItems(null, null);
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
                                        task.addNewAction(context, action);
                                        getActivity().finish();
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
                    if (!Main.checkOrRequestDeviceAdmin(context, getActivity())) return false;
                    final String timeoutChoices[] = new String[]{"15 seconds", "30 seconds", "1 minute", "2 minutes", "5 minutes", "10 minutes"};
                    alert.setItems(timeoutChoices, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            action.setCommand(actionName);
                            action.setData(which);
                            task.addNewAction(context, action);
                            getActivity().finish();
                        }
                    });
                    alert.show();
                    break;
                default:
                    if (preference instanceof PreferenceScreen) return false;
                    action.setCommand(actionName);
                    task.addNewAction(context, action);
                    getActivity().finish();
            }
            return true;
        }

    }

}
