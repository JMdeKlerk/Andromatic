package nz.johannes.andromatic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class TaskListViewAdapter extends ArrayAdapter<Task> {

    public TaskListViewAdapter(Context context, int resource, List<Task> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            convertView = vi.inflate(R.layout.task_row, null);
        }
        Task task = getItem(position);
        if (task != null) {
            TextView taskName = (TextView) convertView.findViewById(R.id.name);
            TextView taskDetails = (TextView) convertView.findViewById(R.id.details);
            if (taskName != null) taskName.setText(task.getName());
            if (taskDetails != null) {
                int triggerCount = task.getTriggers().size();
                int actionCount = task.getActions().size();
                String triggerNeedsAnS = (triggerCount == 1) ? " trigger, " : " triggers, ";
                String actionNeedsAnS = (actionCount == 1) ? " action)" : " actions)";
                taskDetails.setText("(" + triggerCount + triggerNeedsAnS + actionCount + actionNeedsAnS);
            }
        }
        return convertView;
    }

}