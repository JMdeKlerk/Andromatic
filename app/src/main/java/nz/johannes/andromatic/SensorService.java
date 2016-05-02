package nz.johannes.andromatic;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class SensorService extends Service {

    private static Context context;
    private static PowerManager.WakeLock wakeLock;
    private static ShakeSensor shakeSensor = new ShakeSensor();

    @Override
    public void onCreate() {
        super.onCreate();
        this.context = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (wakeLock == null) wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getPackageName());
        if (!wakeLock.isHeld()) wakeLock.acquire();
        if (intent.getBooleanExtra("shake", false)) registerShakeSensor();
        return START_STICKY;
    }

    private void registerShakeSensor() {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(shakeSensor, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void unregisterAll() {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensorManager.unregisterListener(shakeSensor);
    }

    @Override
    public void onDestroy() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }
        unregisterAll();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static class ShakeSensor implements SensorEventListener {

        private long lastShook;
        private float acceleration = 0.00f;
        private float currentAcceleration = SensorManager.GRAVITY_EARTH;
        private float previousAcceleration = SensorManager.GRAVITY_EARTH;

        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            previousAcceleration = currentAcceleration;
            currentAcceleration = (float) Math.sqrt((double) (x * x + y * y + z * z));
            float delta = currentAcceleration - previousAcceleration;
            acceleration = acceleration * 0.9f + delta;
            if (acceleration > 15 && (System.currentTimeMillis() - lastShook > 3000)) {
                for (Task task : Main.getAllStoredTasks(context)) {
                    for (Trigger trigger : task.getTriggers()) {
                        if (trigger.getType().equals("Trigger.Shake")) {
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
}
