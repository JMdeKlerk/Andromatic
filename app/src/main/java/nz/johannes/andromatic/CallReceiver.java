package nz.johannes.andromatic;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;

public class CallReceiver extends BroadcastReceiver {

    private static String lastState;
    private static String lastCaller;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Main.weHavePermission(context, Manifest.permission.READ_PHONE_STATE)) return;
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        if (state.equals(lastState)) return;
        if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
            lastCaller = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            for (Task task : Main.getAllStoredTasks(context)) {
                for (Trigger trigger : task.getTriggers()) {
                    if (trigger.getType().equals("Trigger.AnyIncomingCall")) task.runTask(context);
                    if (trigger.getType().equals("Trigger.IncomingCallByCaller") && PhoneNumberUtils.compare(lastCaller, trigger.getMatch()))
                        task.runTask(context);
                }
            }
        }
        if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK) && lastState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
            for (Task task : Main.getAllStoredTasks(context)) {
                for (Trigger trigger : task.getTriggers()) {
                    if (trigger.getType().equals("Trigger.AnyAnsweredCall")) task.runTask(context);
                    if (trigger.getType().equals("Trigger.AnsweredCallByCaller") && PhoneNumberUtils.compare(lastCaller, trigger.getMatch()))
                        task.runTask(context);
                }
            }
        }
        if (state.equals(TelephonyManager.EXTRA_STATE_IDLE) && lastState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
            for (Task task : Main.getAllStoredTasks(context)) {
                for (Trigger trigger : task.getTriggers()) {
                    if (trigger.getType().equals("Trigger.AnyEndedCall")) task.runTask(context);
                    if (trigger.getType().equals("Trigger.EndedCallByCaller") && PhoneNumberUtils.compare(lastCaller, trigger.getMatch()))
                        task.runTask(context);
                }
            }
        }
        lastState = state;
    }

}
