package nz.johannes.andromatic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Task task = Main.getTask(context, intent.getData().getHost());
            task.runTask(context);
            task.setAlarms(context);
        } catch (NullPointerException e) {
            // Task no longer exists but alarm was not unset
            e.printStackTrace();
        }
    }

}
