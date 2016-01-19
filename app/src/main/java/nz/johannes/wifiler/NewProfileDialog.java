package nz.johannes.wifiler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.google.gson.Gson;

import java.util.ArrayList;

public class NewProfileDialog extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_newprofile);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        this.getWindow().setAttributes(params);
        WifiManager manager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        ArrayList<String> knownNetworks = new ArrayList<>();
        for (WifiConfiguration network : manager.getConfiguredNetworks()) {
            String ssid = network.SSID.replace("\"", "");
            if (!knownNetworks.contains(ssid)) knownNetworks.add(ssid);
        }
        for (ScanResult network : manager.getScanResults()) {
            if (!knownNetworks.contains(network.SSID)) knownNetworks.add(network.SSID);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, knownNetworks);
        AutoCompleteTextView ssidBox = (AutoCompleteTextView) findViewById(R.id.ssid);
        ssidBox.setText(manager.getConnectionInfo().getSSID());
        ssidBox.setAdapter(adapter);
        super.onCreate(savedInstanceState);
    }

    public void confirm(View view) {
        EditText nameField = (EditText) findViewById(R.id.name);
        String name = nameField.getText().toString();
        EditText ssidField = (EditText) findViewById(R.id.ssid);
        String ssid = ssidField.getText().toString();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        Gson gson = new Gson();
        Profile profile = new Profile(name, ssid);
        String storeProfile = gson.toJson(profile);
        editor.putString("profile-" + name, storeProfile).apply();
        Intent returnIntent = new Intent();
        returnIntent.putExtra("profile", name);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    public void cancel(View view) {
        setResult(Activity.RESULT_CANCELED, new Intent());
        finish();
    }

}
