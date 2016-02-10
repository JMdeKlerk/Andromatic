package nz.johannes.wifiler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class ActionListViewAdapter extends ArrayAdapter<Action> {

    public ActionListViewAdapter(Context context, int resource, List<Action> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            convertView = vi.inflate(R.layout.default_list_row, null);
        }
        Action action = getItem(position);
        if (action != null) {
            TextView type = (TextView) convertView.findViewById(R.id.type);
            TextView detail = (TextView) convertView.findViewById(R.id.detail);
            if (type != null) {
                type.setText(action.getCommand());
            }
            if (detail != null) {
                if (action.getData() != -1) detail.append(String.valueOf(action.getData()));
                if (action.getMultiData() != null) detail.append(String.valueOf(action.getMultiData().get(0)));
            }
        }
        return convertView;
    }

}