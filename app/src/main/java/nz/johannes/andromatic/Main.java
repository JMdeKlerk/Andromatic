package nz.johannes.andromatic;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.EditText;
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
        populateTaskList();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.setView(R.layout.dialog_newtask);
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText nameField = (EditText) ((AlertDialog) dialog).findViewById(R.id.task_name);
                        String name = nameField.getText().toString();
                        Task task = new Task(getBaseContext(), name);
                        populateTaskList();
                    }
                });
                alert.setNegativeButton("Cancel", null);
                alert.show();
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
        populateTaskList();
        super.onResume();
    }

    private void populateTaskList() {
        final ListView taskList = (ListView) findViewById(R.id.list);
        final ArrayList<Task> listItems = new ArrayList<>();
        final Task.TaskListViewAdapter adapter = new Task().new TaskListViewAdapter(this, R.layout.task_row, listItems);
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        taskList.setAdapter(adapter);
        taskList.setEmptyView(findViewById(R.id.empty));
        taskList.setClickable(true);
        taskList.setLongClickable(true);
        taskList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent taskEditor = new Intent(getApplicationContext(), TaskEditor.class);
                Task task = (Task) taskList.getItemAtPosition(position);
                String toEdit = task.getName();
                taskEditor.putExtra("task", toEdit);
                startActivity(taskEditor);
            }
        });
        taskList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                alert.setTitle("Delete task?");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Task task = (Task) taskList.getItemAtPosition(position);
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();
                        editor.remove("task-" + task.getName()).apply();
                        populateTaskList();
                    }
                });
                alert.setNegativeButton("No", null);
                alert.show();
                return true;
            }
        });
        for (Task task : getAllStoredTasks(this)) {
            listItems.add(task);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settings = new Intent(this, Settings.class);
            startActivity(settings);
        }
        if (id == R.id.action_exit) finish();
        return super.onOptionsItemSelected(item);
    }

    public static ArrayList<Task> getAllStoredTasks(Context context) {
        ArrayList<Task> tasks = new ArrayList<>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        for (String key : prefs.getAll().keySet()) {
            if (key.startsWith("task-")) {
                Task task = new Gson().fromJson(prefs.getString(key, ""), Task.class);
                tasks.add(task);
            }
        }
        return tasks;
    }

    public static void showToast(Context context, String message) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String duration = prefs.getString("toastDuration", "1");
        if (duration.equals("1")) new Toast(context).makeText(context, message, Toast.LENGTH_SHORT).show();
        if (duration.equals("2")) new Toast(context).makeText(context, message, Toast.LENGTH_LONG).show();
    }

}
