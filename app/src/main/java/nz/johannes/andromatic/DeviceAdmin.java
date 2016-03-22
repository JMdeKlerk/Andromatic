package nz.johannes.andromatic;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

public class DeviceAdmin extends DeviceAdminReceiver {

    @Override
    public void onDisabled(Context context, Intent intent) {
        for (Task task : Main.getAllStoredTasks(context)) {
            for (Action action : task.getActions()) {
                if (action.getCommand().equals("Set lock mode")) task.removeAction(context, action);
            }
        }
    }

}
