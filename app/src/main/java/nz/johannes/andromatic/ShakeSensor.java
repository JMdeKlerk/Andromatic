package nz.johannes.andromatic;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class ShakeSensor implements SensorEventListener {

    private Context context;
    private long lastShook;
    private float acceleration = 0.00f;
    private float currentAcceleration = SensorManager.GRAVITY_EARTH;
    private float previousAcceleration = SensorManager.GRAVITY_EARTH;

    public ShakeSensor(Context context) {
        this.context = context;
        lastShook = System.currentTimeMillis() - 3000;
    }

    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        previousAcceleration = currentAcceleration;
        currentAcceleration = (float) Math.sqrt((double) (x * x + y * y + z * z));
        float delta = currentAcceleration - previousAcceleration;
        acceleration = acceleration * 0.9f + delta;
        if (acceleration > 15) {
            for (Task task : Main.getAllStoredTasks(context)) {
                for (Trigger trigger : task.getTriggers()) {
                    if (trigger.getType().equals("Trigger.Shake") && (System.currentTimeMillis() - lastShook > 3000)) {
                        task.runTask(context);
                        lastShook = System.currentTimeMillis();
                    }
                }
            }
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

}
