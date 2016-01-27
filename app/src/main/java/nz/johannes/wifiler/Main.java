package nz.johannes.wifiler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

public class Main extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        populateProfileList();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent showNewProfileActivity = new Intent(getApplicationContext(), NewProfileDialog.class);
                startActivityForResult(showNewProfileActivity, 1);
            }
        });
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getString("lastConnectedSSID", "").equals("")) {
            SharedPreferences.Editor editor = prefs.edit();
            WifiManager manager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
            String ssid = manager.getConnectionInfo().getSSID().replace("\"", "");
            editor.putString("lastConnectedSSID", ssid).apply();
        }
    }

    @Override
    protected void onResume() {
        populateProfileList();
        super.onResume();
    }

    private void populateProfileList() {
        final ListView profileList = (ListView) findViewById(R.id.list);
        final ArrayList<Profile> listItems = new ArrayList<>();
        final ProfileListViewAdapter adapter = new ProfileListViewAdapter(this, R.layout.profile_row, listItems);
        profileList.setAdapter(adapter);
        profileList.setClickable(true);
        profileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent profileEditor = new Intent(getApplicationContext(), ProfileEditor.class);
                Profile profile = (Profile) profileList.getItemAtPosition(position);
                String toEdit = profile.getName();
                profileEditor.putExtra("profile", toEdit);
                startActivity(profileEditor);
            }
        });
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        for (String key : prefs.getAll().keySet()) {
            if (key.startsWith("profile-")) {
                String profileJson = prefs.getString(key, "");
                Profile profile = new Gson().fromJson(profileJson, Profile.class);
                listItems.add(profile);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Intent profileEditor = new Intent(getApplicationContext(), ProfileEditor.class);
            profileEditor.putExtra("profile", data.getStringExtra("profile"));
            startActivity(profileEditor);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) return true;
        if (id == R.id.action_exit) finish();
        return super.onOptionsItemSelected(item);
    }

    public static void showToast(Context context, String message) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int duration = prefs.getInt("toastDuration", Toast.LENGTH_SHORT);
        if (duration == 0) new Toast(context).makeText(context, message, Toast.LENGTH_SHORT).show();
        if (duration == 1) new Toast(context).makeText(context, message, Toast.LENGTH_LONG).show();
    }

}
