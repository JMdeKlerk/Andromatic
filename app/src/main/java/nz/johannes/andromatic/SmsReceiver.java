package nz.johannes.andromatic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        for (Task task : Main.getAllStoredTasks(context)) {
            for (Trigger trigger : task.getTriggers()) {
                if (trigger.getType().equals("SMS received")) {
                    Bundle bundle = intent.getExtras();
                    Intent fakeIntent = new Intent("android.provider.Telephony.SMS_RECEIVED");
                    fakeIntent.putExtras(bundle);
                    fakeIntent.putExtra("fake", true);
                    if (bundle != null) {
                        Object[] pdus = (Object[]) bundle.get("pdus");
                        SmsMessage[] messages = new SmsMessage[pdus.length];
                        for (int i = 0; i < messages.length; i++) {
                            messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                            String body = messages[i].getMessageBody();
                            if (trigger.getExtraData().get(0).equals("Exact") && body.equals(trigger.getMatch())) {
                                task.runTask(context);
                            }
                            if (trigger.getExtraData().get(0).equals("Partial") && body.contains(trigger.getMatch())) {
                                task.runTask(context);
                            }
                        }
                    }
                }
            }
        }
    }

}
