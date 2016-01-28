package nz.johannes.wifiler;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
        actionList.setAdapter(adapter);
        actionList.setClickable(true);
        actionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                actionToDelete = (Action) actionList.getItemAtPosition(position);
                Intent showActionClickedDialog = new Intent(getApplicationContext(), ActionClickedDialog.class);
                startActivityForResult(showActionClickedDialog, 1);
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
        builder.setTitle("State:");
        builder.setItems(stateChoices, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                action.setRequiredState(i == 0 ? NetworkInfo.State.CONNECTED : NetworkInfo.State.DISCONNECTED);
                builder.setTitle("Action:");
                builder.setItems(actionChoices, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        action.setCommand(actionChoices[i]);
                        profile.addNewAction(getBaseContext(), action);
                        populateActionsList();
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
