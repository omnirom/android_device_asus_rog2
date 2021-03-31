/*
 * Copyright (c) 2021 The OmniRom Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.omnirom.device;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import android.util.Slog;

public class GripSensor {
    private static final boolean DEBUG = false;
    private static final int MSG_BIND_SERVICE = 1001;
    private static final int MSG_REGISTER_GRIPSENSOR = 1000;
    private static final int MSG_RESET_BINDINGSTATE = 1002;
    private static final int MSG_UPDATE_REMOTE_SENSOR_EVENT = 3000;
    public static final int MSG_UPDATE_SENSOR_ON_CHANGED = 2000;
    private static final String REMOTE_SERVICE_BIND_ACTION = "com.asus.focusapplistener.grip.messengerservice";
    private static final int REMOTE_SERVICE_STATE_BINDING = 1;
    private static final int REMOTE_SERVICE_STATE_BOUND = 2;
    private static final int REMOTE_SERVICE_STATE_UNBOUND = 0;
    private static final String REMTOE_SERVICE_PACKAGENAME = "com.asus.focusapplistener";
    private static final String TAG = "GripSensor";
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            synchronized (mLock) {
                Slog.d(TAG, "grip sensor service is connected");
                Messenger unused = mRemoteMsgService = new Messenger(service);
                int unused2 = mServiceState = 2;
                Slog.d(TAG, "service state: " + mServiceState);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            synchronized (mLock) {
                Slog.d(TAG, "grip sensor service is disconnected");
                Messenger unused = mRemoteMsgService = null;
                int unused2 = mServiceState = 0;
            }
        }
    };
    private Context mContext;
    MyGripsensorListener mGripSensorListener;
    private HandlerThread mHandlerThread;
    private Object mLock = new Object();
    private Handler mMainHandler = null;
    private Messenger mRemoteMsgService = null;
    private int mServiceState = 0;
    private WorkerHandler mWorkerHandler = null;

    public GripSensor(Context context) {
        mContext = context;
    }

    public void onStart() {
    if (DEBUG) Log.d(TAG, "Enabling");
    this.mHandlerThread = new HandlerThread("GripSensorServiceHandler", -8);
    this.mHandlerThread.start();
    this.mWorkerHandler = new WorkerHandler(this.mHandlerThread.getLooper(), this.mContext);
    this.mMainHandler = new SensorServiceHandler(this.mContext.getMainLooper());
    this.mMainHandler.sendEmptyMessage(1000);
    Slog.d(TAG, "GripSensor onStart()");
    }

    void disable() {
    }

    private class SensorServiceHandler extends Handler {
        public SensorServiceHandler(Looper l) {
            super(l);
        }

        public void handleMessage(Message msg) {
            int gripMessengerServiceState;
            switch (msg.what) {
                case 1000:
                    GripSensor gripSensorService = GripSensor.this;
                    gripSensorService.mGripSensorListener = new MyGripsensorListener((SensorManager) gripSensorService.mContext.getSystemService("sensor"), 65537, true, GripSensor.this.mWorkerHandler, GripSensor.this.mContext);
                    if (GripSensor.this.mGripSensorListener != null) {
                        GripSensor.this.mGripSensorListener.register();
                    }
                    try {
                        SystemProperties.set("persist.asus.hardware.gripsensor", "1");
                        SystemProperties.set("persist.vendor.asus.hardware.gripsensor", "1");
                    } catch (Exception e) {
                        Log.w(GripSensor.TAG, e.toString());
                    }
                    sendEmptyMessage(1001);
                    return;
                case 1001:
                    synchronized (GripSensor.this.mLock) {
                        gripMessengerServiceState = GripSensor.this.mServiceState;
                        Slog.d(GripSensor.TAG, "Bind service, current state -> " + GripSensor.this.mServiceState);
                    }
                    if (gripMessengerServiceState == 0) {
                        Slog.d(GripSensor.TAG, "Remote service is unbound. Bind it and send message later");
                        synchronized (GripSensor.this.mLock) {
                            GripSensor.this.doBindServiceLocked();
                        }
                        sendMessageDelayed(Message.obtain(msg), 1000);
                        return;
                    } else if (gripMessengerServiceState != 1) {
                        return;
                    } else {
                        if (retryLimitReached(msg.arg1)) {
                            Slog.w(GripSensor.TAG, "Reach max retry times, reset binding state after 30 seconds.");
                            sendEmptyMessageDelayed(GripSensor.MSG_RESET_BINDINGSTATE, 30000);
                            return;
                        }
                        Slog.d(GripSensor.TAG, "Remote service is binding. Send message later.");
                        Message newMessage = Message.obtain(msg);
                        newMessage.arg1++;
                        sendMessageDelayed(newMessage, 1000);
                        return;
                    }
                case GripSensor.MSG_RESET_BINDINGSTATE /*{ENCODED_INT: 1002}*/:
                    synchronized (GripSensor.this.mLock) {
                        GripSensor.this.mServiceState = 0;
                        GripSensor.this.mRemoteMsgService = null;
                        Slog.d(GripSensor.TAG, "Reset state: " + GripSensor.this.mServiceState);
                    }
                    return;
                default:
                    return;
            }
        }

        private boolean retryLimitReached(int count) {
            return count > 9;
        }
    }

    /* access modifiers changed from: private */
    public class WorkerHandler extends Handler {
        public WorkerHandler(Looper l, Context context) {
            super(l);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 2000) {
                synchronized (GripSensor.this.mLock) {
                    GripSensor.this.updateSensorEventToRemoteLocked(msg.getData());
                }
            }
        }
    }

    class MyGripsensorListener extends GripsensorListener {
        MyGripsensorListener(SensorManager sensorManager, int type, boolean wakeUp, Handler handler, Context context) {
            super(sensorManager, type, wakeUp, handler, context);
        }

        @Override // com.android.server.policy.GripsensorListener
        public void onSensorChanged(SensorEvent event) {
            int length = event.values.length;
            Sensor sensor = event.sensor;
            if (length == 0) {
                Log.w(GripSensor.TAG, "Cannot obtain value from sensor " + sensor);
            } else if (event.sensor.getType() == 65537) {
                if (GripSensor.DEBUG) {
                    Slog.d(GripSensor.TAG, "GRIP array:  \nGesture_TYPE[0]: " + event.values[0] + " \nTRK_ID[1]: " + event.values[1] + " \nBAR_ID[2]: " + event.values[2] + " \nPRESSURE[3]: " + event.values[3] + " \nFRAME[4]: " + event.values[4] + " \nCenter[5]: " + event.values[5] + "\nLENGTH[6]: " + event.values[6]);
                }
                acquireWakelock((int) event.values[0]);
                Message msg = this.mHandler.obtainMessage(GripSensor.MSG_UPDATE_SENSOR_ON_CHANGED);
                Bundle data = new Bundle();
                data.putFloatArray("data", (float[]) event.values.clone());
                msg.setData(data);
                this.mHandler.sendMessage(msg);
            }
        }
    }

    private void doBindServiceLocked() {
        Intent intent = new Intent();
        intent.setAction(REMOTE_SERVICE_BIND_ACTION);
        intent.setPackage(REMTOE_SERVICE_PACKAGENAME);
        try {
            this.mContext.bindServiceAsUser(intent, this.mConnection, 1, UserHandle.CURRENT);
            this.mServiceState = 1;
            Slog.d(TAG, "Bind grip sensor service");
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void updateSensorEventToRemoteLocked(Bundle in) {
        if (this.mRemoteMsgService != null) {
            Message msg = Message.obtain((Handler) null, (int) MSG_UPDATE_REMOTE_SENSOR_EVENT);
            try {
                msg.setData(new Bundle(in));
                this.mRemoteMsgService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (this.mServiceState == 0) {
            Slog.e(TAG, "Client object is null. Rebind...");
            this.mMainHandler.sendEmptyMessage(1001);
        }
    }
}
