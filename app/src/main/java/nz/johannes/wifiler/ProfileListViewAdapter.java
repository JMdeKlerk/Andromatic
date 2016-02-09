package nz.johannes.wifiler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ProfileListViewAdapter extends ArrayAdapter<Profile> {

    public ProfileListViewAdapter(Context context, int resource, List<Profile> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            convertView = vi.inflate(R.layout.profile_row, null);
        }
        Profile profile = getItem(position);
        if (profile != null) {
            ImageView activeIcon = (ImageView) convertView.findViewById(R.id.activeIcon);
            TextView profileName = (TextView) convertView.findViewById(R.id.name);
            if (activeIcon != null) {
                activeIcon.setBackgroundResource(profile.isActiveProfile(getContext()) ?
                        android.R.drawable.checkbox_on_background : android.R.drawable.checkbox_off_background);
            }
            if (profileName != null) {
                profileName.setText(profile.getName());
            }
        }
        return convertView;
    }

}