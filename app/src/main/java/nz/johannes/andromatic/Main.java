package nz.johannes.andromatic;

import android.Manifest;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

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
            public void onClick(View v) {
                View view = getLayoutInflater().inflate(R.layout.dialog_singleline, null);
                EditText textView = (EditText) view.findViewById(R.id.text);
                textView.setHint("Name");
                alert.setView(view);
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText nameField = (EditText) ((AlertDialog) dialog).findViewById(R.id.text);
                        String name = nameField.getText().toString();
                        Task task = new Task(getBaseContext(), name);
                        populateTaskList();
                    }
                });
                alert.setNegativeButton("Cancel", null);
                alert.show();
            }
        });
        manageReceivers(this);
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
                        task.unsetAlarms(getBaseContext());
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();
                        editor.remove("task-" + task.getName()).commit();
                        manageReceivers(getBaseContext());
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
        Collections.sort(listItems, new Comparator<Task>() {
            @Override
            public int compare(Task lhs, Task rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });
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

    public static Task getTask(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Task task = new Gson().fromJson(prefs.getString("task-" + key, ""), Task.class);
        return task;
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

    public static void manageReceivers(final Context context) {
        context.stopService(new Intent(context, SensorService.class));
        boolean[] receiverBools = new boolean[6];
        boolean[] sensorBools = new boolean[2];
        PackageManager pm = context.getPackageManager();
        ComponentName[] receivers = new ComponentName[]{new ComponentName(context, AlarmReceiver.class),
                new ComponentName(context, BatteryReceiver.class), new ComponentName(context, BluetoothReceiver.class),
                new ComponentName(context, CallReceiver.class), new ComponentName(context, SmsReceiver.class),
                new ComponentName(context, WifiReceiver.class)};
        for (Task task : getAllStoredTasks(context)) {
            for (Trigger trigger : task.getTriggers()) {
                switch (trigger.getType()) {
                    case "Trigger.Interval":
                    case "Trigger.Time":
                        receiverBools[0] = true;
                        break;
                    case "Trigger.BatteryLow":
                    case "Trigger.ChargerInserted":
                    case "Trigger.ChargerRemoved":
                        receiverBools[1] = true;
                        break;
                    case "Trigger.Bluetooth":
                        receiverBools[2] = true;
                        break;
                    case "Trigger.AnyIncomingCall":
                    case "Trigger.IncomingCallByCaller":
                    case "Trigger.AnyAnsweredCall":
                    case "Trigger.AnsweredCallByCaller":
                    case "Trigger.AnyEndedCall":
                    case "Trigger.EndedCallByCaller":
                        receiverBools[3] = true;
                        break;
                    case "Trigger.AnySMS":
                    case "Trigger.SMSByContent":
                    case "Trigger.SMSBySender":
                        receiverBools[4] = true;
                        break;
                    case "Trigger.WifiConnected":
                    case "Trigger.WifiConnectedBySSID":
                    case "Trigger.WifiDisconnected":
                    case "Trigger.WifiDisconnectedBySSID":
                        receiverBools[5] = true;
                        break;
                    case "Trigger.Shake":
                        sensorBools[0] = true;
                        break;
                    case "Trigger.Flip":
                    case "Trigger.FaceUp":
                    case "Trigger.FaceDown":
                        sensorBools[1] = true;
                        break;
                }
            }
            for (Condition condition : task.getConditions()) {
                if (condition.getType().equals("Condition.FaceUp") || condition.getType().equals("Condition.FaceDown"))
                    sensorBools[1] = true;
            }
        }
        for (int i = 0; i < receivers.length; i++) {
            if (receiverBools[i])
                pm.setComponentEnabledSetting(receivers[i], PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            else pm.setComponentEnabledSetting(receivers[i], PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
        Intent intent = new Intent(context, SensorService.class);
        intent.putExtra("shake", sensorBools[0]);
        intent.putExtra("flip", sensorBools[1]);
        if (sensorBools[0] || sensorBools[1]) context.startService(intent);
    }

    public static void showToast(Context context, String message) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String duration = prefs.getString("toastDuration", "1");
        if (duration.equals("1")) new Toast(context).makeText(context, message, Toast.LENGTH_SHORT).show();
        if (duration.equals("2")) new Toast(context).makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static String getNameFromNumber(Context context, String number) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) return null;
        String contactName = null;
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }
        cursor.close();
        return contactName;
    }

    public static boolean weHavePermission(Context context, String permission) {
        if (permission.equals("device_admin")) {
            DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName component = new ComponentName(context, DeviceAdmin.class);
            return dpm.isAdminActive(component);
        }
        if (android.os.Build.VERSION.SDK_INT < 23) return true;
        return (ContextCompat.checkSelfPermission(context, permission) == 0);
    }

    public static ArrayAdapter getTextViewAdapter(Context context, String type) {
        switch (type) {
            case "bluetooth":
                BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
                ArrayList<String> devices = new ArrayList<>();
                for (BluetoothDevice device : bluetooth.getBondedDevices()) {
                    devices.add(device.getName());
                }
                String[] devicesArray = new String[devices.size()];
                devices.toArray(devicesArray);
                return new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, devicesArray);
            case "contacts":
                if (!weHavePermission(context, Manifest.permission.READ_CONTACTS)) return null;
                ArrayList<String> contacts = new ArrayList<>();
                Cursor people = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
                while (people.moveToNext()) {
                    String contactName = people.getString(people.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    String contactId = people.getString(people.getColumnIndex(ContactsContract.Contacts._ID));
                    Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                    while (phones.moveToNext()) {
                        String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        contacts.add(contactName + " (" + phoneNumber + ")");
                    }
                    phones.close();
                }
                people.close();
                return new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, contacts);
            case "ssids":
                WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                ArrayList<String> networks = new ArrayList<>();
                if (wifi.getConfiguredNetworks() == null) return null;
                for (WifiConfiguration network : wifi.getConfiguredNetworks()) {
                    String ssid = network.SSID.replace("\"", "");
                    if (!networks.contains(ssid)) networks.add(ssid);
                }
                for (ScanResult network : wifi.getScanResults()) {
                    String ssid = network.SSID;
                    if (!networks.contains(ssid)) networks.add(ssid);
                }
                String[] networksArray = new String[networks.size()];
                networks.toArray(networksArray);
                return new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, networksArray);
            default:
                return null;
        }
    }

}
