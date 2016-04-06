package nz.johannes.andromatic;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.WindowManager;

public class AddComponent extends PreferenceActivity {

    private static Task task;
    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;
        task = Main.getTask(this, getIntent().getStringExtra("Task"));
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        this.getWindow().setAttributes(params);
        String componentType = getIntent().getAction();
        switch (componentType) {
            case "TRIGGER":
                getFragmentManager().beginTransaction().replace(android.R.id.content, new TriggerFragment()).commit();
                break;
            case "CONDITION":
                break;
            case "ACTION":
                getFragmentManager().beginTransaction().replace(android.R.id.content, new ActionFragment()).commit();
                break;
        }
    }

    public static class TriggerFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.triggers);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (preference instanceof PreferenceScreen) return false;
            final Trigger trigger = new Trigger();
            final String triggerKey = preference.getKey();
            switch (triggerKey) {
                case "Trigger.IncomingCallByCaller":
                    // TODO
                    break;
                case "Trigger.AnsweredCallByCaller":
                    // TODO
                    break;
                case "Trigger.EndedCallByCaller":
                    // TODO
                    break;
                case "Trigger.SMSByContent":
                    // TODO
                    break;
                case "Trigger.SMSBySender":
                    // TODO
                    break;
                case "Trigger.Interval":
                    // TODO
                    break;
                case "Trigger.Time":
                    // TODO
                    break;
                case "Trigger.Bluetooth":
                    // TODO
                    break;
                case "Trigger.WifiConnected":
                    // TODO
                    break;
                case "Trigger.WifiDisconnected":
                    // TODO
                    break;
                default:
                    trigger.setType(triggerKey);
                    task.addNewTrigger(context, trigger);
                    getActivity().finish();
            }
            return true;
        }

    }

    public static class ActionFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.actions);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (preference instanceof PreferenceScreen) return false;
            final Action action = new Action();
            final String actionType = preference.getKey();
            switch (actionType) {
                case "Action.StartCall":
                    // TODO
                    break;
                case "Action.SendSMS":
                    // TODO
                    break;
                case "Action.MediaVolume":
                    // TODO
                    break;
                case "Action.LaunchApp":
                    // TODO
                    break;
                case "Action.PlaySound":
                    // TODO
                    break;
                case "Action.LockModePIN":
                    // TODO
                    break;
                case "Action.LockModePassword":
                    // TODO
                    break;
                case "Action.LockModeNone":
                case "Action.Timeout15Sec":
                case "Action.Timeout30Sec":
                case "Action.Timeout1Min":
                case "Action.Timeout2Min":
                case "Action.Timeout5Min":
                case "Action.Timeout10Min":
                    Main.checkOrRequestDeviceAdmin(context, getActivity());
                    break;
                case "Action.RingerVolume":
                    // TODO
                    break;
                case "Action.NotificationVolume":
                    // TODO
                    break;
                default:
                    action.setCommand(actionType);
                    task.addNewAction(context, action);
                    getActivity().finish();
            }
            return true;
        }

    }

}
