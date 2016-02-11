package nz.johannes.andromatic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class TriggerListViewAdapter extends ArrayAdapter<Trigger> {

    public TriggerListViewAdapter(Context context, int resource, List<Trigger> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            convertView = vi.inflate(R.layout.default_list_row, null);
        }
        Trigger trigger = getItem(position);
        if (trigger != null) {
            TextView type = (TextView) convertView.findViewById(R.id.type);
            if (type != null) {
                type.setText(trigger.getType());
                if (trigger.getMatch() != null) type.append(": " + trigger.getMatch());
            }
        }
        return convertView;
    }

}