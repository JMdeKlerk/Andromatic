package nz.johannes.andromatic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class HeadphoneReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (isInitialStickyBroadcast()) return;
        String action = (intent.getIntExtra("state", -1) == 1) ? "Headphones inserted" : "Headphones removed";
        for (Task task : Main.getAllStoredTasks(context)) {
            for (Trigger trigger : task.getTriggers()) {
                if (trigger.getType().equals(action)) task.runTask(context);
            }
        }
    }

}
