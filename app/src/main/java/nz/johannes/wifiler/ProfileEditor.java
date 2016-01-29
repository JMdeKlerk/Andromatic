package nz.johannes.wifiler;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class ProfileEditor extends AppCompatActivity {

    private Profile profile;
    private Action actionToDelete;

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
        final ListView actionList = (ListView) findViewById(R.id.list);
        final ArrayList<Action> listItems = new ArrayList<>();
        final ActionListViewAdapter adapter = new ActionListViewAdapter(this, R.layout.action_row, listItems);
        registerForContextMenu(actionList);
        actionList.setAdapter(adapter);
        actionList.setClickable(true);
        actionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                actionToDelete = (Action) actionList.getItemAtPosition(position);
                profile.removeAction(getBaseContext(), actionToDelete);
                populateActionsList();
            }
        });
        for (Action action : profile.getActions()) {
            listItems.add(action);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            profile.removeAction(this, actionToDelete);
        }
    }

    public void addTrigger() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final Action action = new Action();
        final String stateChoices[] = new String[]{"On connect...", "On disconnect..."};
        final String actionChoices[] = new String[]{"Launch app", "Kill app", "Enable wifi", "Disable wifi", "Enable bluetooth", "Disable bluetooth",
                "Enable mobile data", "Disable mobile data", "Enable GPS", "Disable GPS", "Set brightness", "Set ringer volume", "Set media volume",
                "Set lock mode", "Send SMS", "Send email", "Start timer", "Stop timer", "Shut down phone", "Play sound"};
        builder.setItems(stateChoices, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                action.setRequiredState(i == 0 ? NetworkInfo.State.CONNECTED : NetworkInfo.State.DISCONNECTED);
                builder.setItems(actionChoices, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        final String actionChoice = actionChoices[i];
                        switch (actionChoice) {
                            case "Launch app":
                            case "Kill app":
                                final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                                final String[] appChoices = new String[999];
                                final List<ResolveInfo> appList = getBaseContext().getPackageManager().queryIntentActivities(mainIntent, 0);
                                for (int x = 0; x < appList.size(); x++) {
                                        appChoices[x] = appList.get(x).activityInfo.applicationInfo.loadLabel(getPackageManager()).toString();
                                }
                                builder.setItems(appChoices, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int i) {
                                        action.setCommand(actionChoice);
                                        action.setData(appChoices[i]);
                                        profile.addNewAction(getBaseContext(), action);
                                        populateActionsList();
                                    }
                                });
                                builder.show();
                                break;
                            case "Set brightness":
                            case "Set ringer volume":
                            case "Set media volume":
                                // Choose via bar
                                break;
                            case "Send SMS":
                            case "Send email":
                                // Choose recipient, contents
                                break;
                            case "Set lock mode":
                                final String lockChoices[] = new String[]{"None", "PIN", "Gesture", "Fingerprint"};
                                builder.setItems(lockChoices, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int i) {
                                        action.setCommand(actionChoice);
                                        action.setData(lockChoices[i]);
                                        profile.addNewAction(getBaseContext(), action);
                                        populateActionsList();
                                    }
                                });
                                builder.show();
                                break;
                            case "Play sound":
                                // Choose file
                                break;
                            default:
                                action.setCommand(actionChoice);
                                profile.addNewAction(getBaseContext(), action);
                                populateActionsList();
                                break;
                        }
                    }
                });
                builder.show();
            }
        });
        builder.show();
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
        if (id == R.id.action_add) addTrigger();
        if (id == R.id.action_delete) deleteProfile();
        return super.onOptionsItemSelected(item);
    }

}
