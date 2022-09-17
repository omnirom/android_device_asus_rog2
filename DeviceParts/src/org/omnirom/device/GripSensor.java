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
    private static final boolean DEBUG;
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
    private static final String TAG = "GripSensorService";
    private Context mContext;
    MyGripSensorListener mGripSensorListener;
    private HandlerThread mHandlerThread;
    private int mServiceState = 0;
    private Handler mMainHandler = null;
    private Messenger mRemoteMsgService = null;
    private Object mLock = new Object();
    private WorkerHandler mWorkerHandler = null;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            synchronized (mLock) {
                Slog.d(TAG, "grip sensor service is connected");
                mRemoteMsgService = new Messenger(service);
                mServiceState = 2;
                Slog.d(TAG, "service state: " + mServiceState);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            synchronized (mLock) {
                Slog.d(TAG, "grip sensor service is disconnected");
                mRemoteMsgService = null;
                mServiceState = 0;
            }
        }
    };

    static {
        boolean z = false;
        if (SystemProperties.getInt("persist.vendor.asus.grip.debug", 0) == 1) {
            z = true;
        }
        DEBUG = z;
    }

    public GripSensor(Context context) {
        mContext = context;
    }

    void disable() {
    }
    
    public void onStart() {
        Slog.d(TAG, "GripSensorService onStart()");
        HandlerThread handlerThread = new HandlerThread("GripSensorServiceHandler", -8);
        mHandlerThread = handlerThread;
        handlerThread.start();
        mWorkerHandler = new WorkerHandler(mHandlerThread.getLooper(), mContext);
        SensorServiceHandler sensorServiceHandler = new SensorServiceHandler(mContext.getMainLooper());
        mMainHandler = sensorServiceHandler;
        sensorServiceHandler.sendEmptyMessage(1000);
    }

    private class SensorServiceHandler extends Handler {
        public SensorServiceHandler(Looper l) {
            super(l);
        }

        @Override
        public void handleMessage(Message msg) {
            int gripMessengerServiceState;
            switch (msg.what) {
                case 1000:
                    mGripSensorListener = new MyGripSensorListener((SensorManager) mContext.getSystemService("sensor"), 65537, true, mWorkerHandler, mContext);
                    if (mGripSensorListener != null) {
                        mGripSensorListener.register();
                    }
                    sendEmptyMessage(1001);
                    return;
                case 1001:
                    synchronized (mLock) {
                        gripMessengerServiceState = mServiceState;
                        Slog.d(TAG, "Bind service, current state -> " + mServiceState);
                    }
                    if (gripMessengerServiceState == 0) {
                        Slog.d(TAG, "Remote service is unbound. Bind it and send message later");
                        synchronized (mLock) {
                            doBindServiceLocked();
                        }
                        sendMessageDelayed(Message.obtain(msg), 1000L);
                        return;
                    } else if (gripMessengerServiceState == 1) {
                        if (retryLimitReached(msg.arg1)) {
                            Slog.w(TAG, "Reach max retry times, reset binding state after 30 seconds.");
                            sendEmptyMessageDelayed(1002, 30000L);
                            return;
                        }
                        Slog.d(TAG, "Remote service is binding. Send message later.");
                        Message newMessage = Message.obtain(msg);
                        newMessage.arg1++;
                        sendMessageDelayed(newMessage, 1000L);
                        return;
                    } else {
                        return;
                    }
                case 1002:
                    synchronized (mLock) {
                        mServiceState = 0;
                        mRemoteMsgService = null;
                        Slog.d(TAG, "Reset state: " + mServiceState);
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

    private class WorkerHandler extends Handler {
        public WorkerHandler(Looper l, Context context) {
            super(l);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_SENSOR_ON_CHANGED:
                    synchronized (mLock) {
                        updateSensorEventToRemoteLocked(msg.getData());
                    }
                    return;
                default:
                    return;
            }
        }
    }

    static class MyGripSensorListener extends GripSensorListener {
        MyGripSensorListener(SensorManager sensorManager, int type, boolean wakeUp, Handler handler, Context context) {
            super(sensorManager, type, wakeUp, handler, context);
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            int length = event.values.length;
            Sensor sensor = event.sensor;
            if (length == 0) {
                Log.w(TAG, "Cannot obtain value from sensor " + sensor);
            } else if (event.sensor.getType() == 65537) {
                if (DEBUG) {
                    Slog.d(TAG, "GRIP array:  \nGesture_TYPE[0]: " + event.values[0] +
                                                  " \nTRK_ID[1]: " + event.values[1] +
                                                  " \nBAR_ID[2]: " + event.values[2] +
                                                  " \nPRESSURE[3]: " + event.values[3] +
                                                  " \nFRAME[4]: " + event.values[4] +
                                                  " \nCenter[5]: " + event.values[5] +
                                                  "\nLENGTH[6]: " + event.values[6]);
                }
                acquireWakelock((int) event.values[0]);
                float[] rawData = (float[]) event.values.clone();
                Message msg = mHandler.obtainMessage(MSG_UPDATE_SENSOR_ON_CHANGED);
                Bundle data = new Bundle();
                data.putFloatArray("data", rawData);
                msg.setData(data);
                mHandler.sendMessage(msg);
            }
        }
    }

    private void doBindServiceLocked() {
        Intent intent = new Intent();
        intent.setAction(REMOTE_SERVICE_BIND_ACTION);
        intent.setPackage(REMTOE_SERVICE_PACKAGENAME);
        try {
            mContext.bindServiceAsUser(intent, mConnection, 1, UserHandle.CURRENT);
            mServiceState = 1;
            Slog.d(TAG, "Bind grip sensor service");
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void updateSensorEventToRemoteLocked(Bundle in) {
        if (mRemoteMsgService != null) {
            Message msg = Message.obtain((Handler) null, (int) MSG_UPDATE_REMOTE_SENSOR_EVENT);
            try {
                Bundle out = new Bundle(in);
                msg.setData(out);
                mRemoteMsgService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (mServiceState == 0) {
            Slog.e(TAG, "Client object is null. Rebind...");
            mMainHandler.sendEmptyMessage(1001);
        }
    }
}
