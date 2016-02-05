package nz.johannes.wifiler;

import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class ProfileEditor extends AppCompatActivity {

    private Profile profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profileedit);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String profileName = getIntent().getStringExtra("profile");
        profile = new Gson().fromJson(prefs.getString("profile-" + profileName, ""), Profile.class);
        setTitle(profile.getName());
        populateActionsList();
    }

    private void populateActionsList() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final ListView actionList = (ListView) findViewById(R.id.list);
        final ArrayList<Action> listItems = new ArrayList<>();
        final ActionListViewAdapter adapter = new ActionListViewAdapter(this, R.layout.action_row, listItems);
        registerForContextMenu(actionList);
        actionList.setAdapter(adapter);
        actionList.setClickable(true);
        actionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Action actionToDelete = (Action) actionList.getItemAtPosition(position);
                alert.setTitle("Delete action?");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        profile.removeAction(getBaseContext(), actionToDelete);
                        populateActionsList();
                    }
                });
                alert.setNegativeButton("No", null);
                alert.show();
            }
        });
        for (Action action : profile.getActions()) {
            listItems.add(action);
        }
        adapter.notifyDataSetChanged();
    }

    public void addTrigger() {

    }

    public void addAction() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final Action action = new Action();
        final String stateChoices[] = new String[]{"On connect...", "On disconnect..."};
        final String actionChoices[] = new String[]{"Launch app", "Kill app", "Enable wifi", "Disable wifi", "Enable bluetooth", "Disable bluetooth",
                "Set brightness", "Set ringer volume", "Set notification volume", "Set media volume", "Set lock mode", "Send SMS", "Send email"};
        alert.setItems(stateChoices, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                action.setRequiredState(which == 0 ? NetworkInfo.State.CONNECTED : NetworkInfo.State.DISCONNECTED);
                alert.setItems(actionChoices, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String actionChoice = actionChoices[which];
                        switch (actionChoice) {
                            case "Launch app":
                            case "Kill app":
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
                                        profile.addNewAction(getBaseContext(), action);
                                        populateActionsList();
                                    }
                                });
                                alert.show();
                                break;
                            case "Set brightness":
                            case "Set ringer volume":
                            case "Set notification volume":
                            case "Set media volume":
                                alert.setItems(null, null);
                                alert.setView(R.layout.dialog_seekbar);
                                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        action.setCommand(actionChoice);
                                        SeekBar seek = (SeekBar) ((AlertDialog) dialog).findViewById(R.id.seek);
                                        action.setData(seek.getProgress() * 10);
                                        profile.addNewAction(getBaseContext(), action);
                                        populateActionsList();
                                    }
                                });
                                alert.setNegativeButton("Cancel", null);
                                alert.show();
                                break;
                            case "Send SMS":
                            case "Send email":
                                alert.setItems(null, null);
                                alert.setView(R.layout.dialog_sendmessage);
                                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        action.setCommand(actionChoice);
                                        EditText to = (EditText) ((AlertDialog) dialog).findViewById(R.id.to);
                                        EditText message = (EditText) ((AlertDialog) dialog).findViewById(R.id.content);
                                        ArrayList actionData = new ArrayList();
                                        actionData.add("Some loser");
                                        actionData.add(to.getText().toString());
                                        actionData.add(message.getText().toString());
                                        action.setData(actionData);
                                        profile.addNewAction(getBaseContext(), action);
                                        populateActionsList();
                                    }
                                });
                                alert.setNegativeButton("Cancel", null);
                                alert.show();
                                break;
                            case "Set lock mode":
                                final String lockChoices[] = new String[]{"None", "PIN", "Gesture", "Fingerprint"};
                                alert.setItems(lockChoices, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        action.setCommand(actionChoice);
                                        action.setData(which);
                                        profile.addNewAction(getBaseContext(), action);
                                        populateActionsList();
                                    }
                                });
                                alert.show();
                                break;
                            default:
                                action.setCommand(actionChoice);
                                profile.addNewAction(getBaseContext(), action);
                                populateActionsList();
                                break;
                        }
                    }
                });
                alert.show();
            }
        });
        alert.show();
    }

    public void deleteProfile() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.remove("profile-" + profile.getName()).apply();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profileedit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_addTrigger) addTrigger();
        if (id == R.id.action_addAction) addAction();
        if (id == R.id.action_delete) deleteProfile();
        return super.onOptionsItemSelected(item);
    }

}
