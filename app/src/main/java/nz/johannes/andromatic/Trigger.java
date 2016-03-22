package nz.johannes.andromatic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class Trigger {

    private String type;
    private String match;
    private ArrayList<String> extraData;

    public Trigger() {

    }

    public Trigger(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String getMatch() {
        return match;
    }

    public void setMatch(String match) {
        this.match = match;
    }

    public ArrayList<String> getExtraData() {
        return extraData;
    }

    public void setExtraData(ArrayList data) {
        this.extraData = data;
    }

    public class TriggerListViewAdapter extends ArrayAdapter<Trigger> {

        public TriggerListViewAdapter(Context context, int resource, List<Trigger> items) {
            super(context, resource, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Trigger trigger = getItem(position);
            if (convertView == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                convertView = (trigger.getMatch() != null) ?
                        vi.inflate(R.layout.default_list_row, null) : vi.inflate(R.layout.list_row_nodetail, null);
            }
            if (trigger != null) {
                TextView type = (TextView) convertView.findViewById(R.id.type);
                TextView detail = (TextView) convertView.findViewById(R.id.detail);
                if (type != null) {
                    type.setText(trigger.getType());
                    if (trigger.getMatch() != null) switch (trigger.getType()) {
                        case "Bluetooth connected":
                        case "Bluetooth disconnected":
                            detail.setText("Device name: " + trigger.getMatch());
                            break;
                        case "Interval":
                        case "Time":
                            detail.setText(trigger.getMatch());
                            break;
                        case "SMS received":
                            if (trigger.getExtraData().get(0).equals("Sender")) {
                                if (trigger.getExtraData().get(1) != null)
                                    detail.setText(trigger.getExtraData().get(1) + " (" + trigger.getMatch() + ")");
                                else detail.setText(trigger.getMatch());
                            } else detail.setText(trigger.getExtraData().get(1) + " match: \"" + trigger.getMatch() + "\"");
                            break;
                        case "Wifi connected":
                        case "Wifi disconnected":
                            detail.setText("SSID: " + trigger.getMatch());
                            break;
                    }
                }
            }
            return convertView;
        }
    }

}
