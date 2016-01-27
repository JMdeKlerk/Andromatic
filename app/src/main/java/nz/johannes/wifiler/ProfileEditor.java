package nz.johannes.wifiler;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
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

    public void addTrigger(View view) {
        Action action = new Action("SET_BLUETOOTH", "enabled", NetworkInfo.State.CONNECTED);
        profile.addNewAction(this, action);
        //TODO
    }

    public void deleteProfile(View view) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.remove("profile-" + profile.getName()).apply();
        finish();
    }

}
