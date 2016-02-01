package nz.johannes.wifiler;

import android.content.Context;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
            convertView = vi.inflate(R.layout.action_row, null);
        }
        Action action = getItem(position);
        if (action != null) {
            TextView command = (TextView) convertView.findViewById(R.id.command);
            TextView trigger = (TextView) convertView.findViewById(R.id.trigger);
            if (command != null) {
                command.setText(action.getCommand());
                if (action.getData() != null) command.append(": " + action.getData());
                if (action.getMultiData() != null) command.append(": " + action.getMultiData().get(0));
            }
            if (trigger != null) {
                trigger.setText(action.getRequiredState().equals(NetworkInfo.State.CONNECTED) ? "On connect:" : "On disconnect:");
            }
        }
        return convertView;
    }

}