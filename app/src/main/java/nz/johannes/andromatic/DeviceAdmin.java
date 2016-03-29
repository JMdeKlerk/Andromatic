package nz.johannes.andromatic;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

public class DeviceAdmin extends DeviceAdminReceiver {

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return "Disabling device admin will remove all triggers and actions relating to screen timeout and lock.";
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        for (Task task : Main.getAllStoredTasks(context)) {
            for (Action action : task.getActions()) {
                if (action.getCommand().equals("Set lock mode") ||
                        action.getCommand().equals("Set screen timeout")) task.removeAction(context, action);
            }
        }
    }

}
