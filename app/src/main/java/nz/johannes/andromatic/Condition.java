package nz.johannes.andromatic;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class Condition {

    private String type;
    private String match;

    public Condition() {

    }

    public boolean check(Context context) {
        switch (type) {
            case "Condition.BatteryPercentage":
                break;
            case "Condition.PhoneCharging":
                Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
            case "Condition.PhoneNotCharging":
                intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                return !(plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB);
            case "Condition.TimePeriod":
                break;
            case "Condition.WifiConnected":
                ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                return wifiInfo.isConnected();
            case "Condition.WifiNotConnected":
                connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                return !wifiInfo.isConnected();
        }
        return false;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMatch() {
        return this.match;
    }

    public void setMatch(String match) {
        this.match = match;
    }

    public class ConditionListViewAdapter extends ArrayAdapter<Condition> {

        public ConditionListViewAdapter(Context context, int resource, List<Condition> items) {
            super(context, resource, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Condition condition = getItem(position);
            if (convertView == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                convertView = (condition.getMatch() != null) ?
                        vi.inflate(R.layout.default_list_row, null) : vi.inflate(R.layout.list_row_nodetail, null);
            }
            if (condition != null) {
                TextView type = (TextView) convertView.findViewById(R.id.type);
                TextView detail = (TextView) convertView.findViewById(R.id.detail);
                // TODO make human readable with per-command details
                type.setText(condition.getType());
            }
            return convertView;
        }
    }

}
