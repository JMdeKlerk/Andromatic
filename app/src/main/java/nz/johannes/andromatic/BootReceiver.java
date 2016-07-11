package nz.johannes.andromatic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        for (Task task : Main.getAllStoredTasks(context)) {
            Log.i("Log", "Managing task " + task.getName());
            Main.manageReceivers(context);
            task.setAlarms(context);
        }
    }

}
