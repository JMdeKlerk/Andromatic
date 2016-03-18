package nz.johannes.andromatic;

import android.content.Context;
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

    public Condition(String type) {
        this.type = type;
    }

    public boolean check() {
        return true;
    }

    public String getType() {
        return this.type;
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
                if (type != null) {
                    type.setText(condition.getType());

                }
            }
            return convertView;
        }
    }

}
