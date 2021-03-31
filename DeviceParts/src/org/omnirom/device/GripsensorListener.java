package org.omnirom.device;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.input.InputManager;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import android.view.KeyEvent;

public abstract class GripsensorListener implements SensorEventListener {
    private static final int Bar0_L1 = 0;
    private static final int Bar1_R1 = 1;
    private static final int Bar2_R2 = 2;
    private static final int DEFAULT_GRIP_MIN_RAW_PRESSURE_SENDOUT = 10;
    private static final int GRIP_GESTURE_AVAILABLE_SQUEEZE = 11;
    private static final int GRIP_GESTURE_CANCEL_SQUEEZE = 10;
    private static final int GRIP_GESTURE_LONG_SQUEEZE = 8;
    private static final int GRIP_GESTURE_NONE = 0;
    private static final int GRIP_GESTURE_SHORT_SQUEEZE = 7;
    private static final int GRIP_GESTURE_START_SQUEEZE = 9;
    private static final int GRIP_GESTURE_TAP_L1 = 1;
    private static final int GRIP_GESTURE_TAP_R1 = 2;
    private static final int GRIP_GESTURE_TAP_R2 = 3;
    private static final int GRIP_TAP_LONGPRESS_STOP_NUM = 1000;
    private static final int GRIP_TAP_LONGPRESS_THRESHOLD = 20;
    private static final boolean LOG = SystemProperties.getBoolean("debug.Gripsensor.log", false);
    private static final String TAG = "GripsensorListener";
    private Context mContext;
    protected Handler mHandler = null;
    private boolean mIsRegistered = false;
    private int mLastL1Pressure = 0;
    private int mLastR1Pressure = 0;
    private int mLastR2Pressure = 0;
    private PowerManager mPowerManager;
    private Sensor mSensor = null;
    private SensorManager mSensorManager = null;
    private int mType;
    private PowerManager.WakeLock mWakeLock;

    protected GripsensorListener(SensorManager sensorManager, int type, boolean wakeUp, Handler handler, Context context) {
        this.mSensorManager = sensorManager;
        this.mType = type;
        this.mContext = context;
        this.mSensor = this.mSensorManager.getDefaultSensor(type);
        if (this.mSensor == null) {
            Log.w(TAG, "Can not get sensor: " + type + ". wakeUp: " + wakeUp);
        }
        Context context2 = this.mContext;
        if (context2 != null && this.mPowerManager == null) {
            this.mPowerManager = (PowerManager) context2.getSystemService("power");
        }
        PowerManager powerManager = this.mPowerManager;
        if (powerManager != null && this.mWakeLock == null) {
            this.mWakeLock = powerManager.newWakeLock(1, "GripSensor.WakeLock");
        }
        this.mHandler = handler;
    }

    public boolean register() {
        Sensor sensor;
        if (this.mIsRegistered || (sensor = this.mSensor) == null) {
            return false;
        }
        this.mIsRegistered = this.mSensorManager.registerListener(this, sensor, 3, this.mHandler);
        return this.mIsRegistered;
    }

    public void unregister() {
        Sensor sensor;
        if (this.mIsRegistered && (sensor = this.mSensor) != null) {
            this.mSensorManager.unregisterListener(this, sensor);
            this.mIsRegistered = false;
        }
    }

    public void onSensorChanged(SensorEvent event) {
        int length = event.values.length;
        Sensor sensor = event.sensor;
        if (length == 0) {
            Log.w(TAG, "Cannot obtain value from sensor " + sensor);
        } else if (event.sensor.getType() == 65537) {
            if (LOG) {
                Slog.d(TAG, "LIGHT arrary:  \nGesture_TYPE[0]: " + event.values[0] + " \nTRK_ID[1]: " + event.values[1] + " \nBAR_ID[2]: " + event.values[2] + " \nPRESSURE[3]: " + event.values[3] + " \nFRAME[4]: " + event.values[4] + " \nCenter[5]: " + event.values[5] + "\nLENGTH[6]: " + event.values[6]);
            }
            acquireWakelock((int) event.values[0]);
            translateToKeyEvent((int) event.values[0], (int) event.values[3], (int) event.values[2], (int) event.values[5], (int) event.values[1]);
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public Sensor getSensor() {
        return this.mSensor;
    }

    private void translateToKeyEvent(int gestureType, int pressure, int barId, int center, int trkId) {
        int keyCodeAction = 1;
        if (gestureType != 0) {
            if (gestureType == 1) {
                if (trkId != 1) {
                    keyCodeAction = 0;
                }
                injectGestureKeycode(843, pressure, false, keyCodeAction);
            } else if (gestureType == 2) {
                if (trkId != 1) {
                    keyCodeAction = 0;
                }
                injectGestureKeycode(844, pressure, false, keyCodeAction);
            } else if (gestureType != 3) {
                switch (gestureType) {
                    case 7:
                        injectGestureKeycode(846, pressure, false, -1);
                        return;
                    case 8:
                        injectGestureKeycode(847, pressure, false, -1);
                        return;
                    case 9:
                        injectGestureKeycode(854, pressure, false, -1);
                        return;
                    case 10:
                        injectGestureKeycode(855, pressure, false, -1);
                        return;
                    case 11:
                        injectGestureKeycode(866, pressure, false, -1);
                        return;
                    default:
                        return;
                }
            } else {
                if (trkId != 1) {
                    keyCodeAction = 0;
                }
                injectGestureKeycode(845, pressure, false, keyCodeAction);
            }
        } else if (barId == 0) {
            if (this.mLastL1Pressure != pressure) {
                this.mLastL1Pressure = pressure;
                injectRawKeycode(848, pressure);
            }
        } else if (barId == 1) {
            if (this.mLastR1Pressure != pressure) {
                this.mLastR1Pressure = pressure;
                injectRawKeycode(849, pressure);
            }
        } else if (barId == 2 && this.mLastR2Pressure != pressure) {
            this.mLastR2Pressure = pressure;
            injectRawKeycode(850, pressure);
        }
    }

    /* access modifiers changed from: protected */
    public void acquireWakelock(int gestureType) {
        PowerManager.WakeLock wakeLock;
        if ((gestureType == 7 || gestureType == 8) && (wakeLock = this.mWakeLock) != null && !wakeLock.isHeld()) {
            this.mWakeLock.acquire(1000);
        }
    }

    private void injectGestureKeycode(int keyCode, int pressure, boolean longpress, int action) {
        long now = SystemClock.uptimeMillis();
        if (action == -1) {
            InputManager.getInstance().injectInputEvent(new KeyEvent(now, now, 0, keyCode, 0, pressure, -1, 0, longpress ? 128 : 0, 257), 0);
            InputManager.getInstance().injectInputEvent(new KeyEvent(now, now, 1, keyCode, 0, pressure, -1, 0, 0, 257), 0);
            return;
        }
        InputManager.getInstance().injectInputEvent(new KeyEvent(now, now, action, keyCode, 0, pressure, -1, 0, longpress ? 128 : 0, 257), 0);
    }

    private void injectRawKeycode(int keyCode, int pressure) {
        long now = SystemClock.uptimeMillis();
        InputManager.getInstance().injectInputEvent(new KeyEvent(now, now, 0, keyCode, 0, pressure, -1, 0, 0, 257), 0);
        InputManager.getInstance().injectInputEvent(new KeyEvent(now, now, 1, keyCode, 0, pressure, -1, 0, 0, 257), 0);
    }
}
