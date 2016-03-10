package nz.johannes.andromatic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Task task = Main.getTask(context, intent.getData().getHost());
        task.runTask(context);
        task.setAlarms(context);
    }

}