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
    private static final boolean DEBUG = true;
    private static final String REMOTE_SERVICE_BIND_ACTION = "com.asus.focusapplistener.grip.messengerservice";
    private static final String REMTOE_SERVICE_PACKAGENAME = "com.asus.focusapplistener";
    private static final String REMOTE_BROADCAST_INTENT_ACTION = "com.asus.asuspointerlistener.intent.action.MONITOR_POINT_EVENT";
    private static final String REMOTE_BROADCAST_INTENT_CATEGORY = "com.asus.asuspointerlistener.intent.category.MONITOR_POINT_EVENT";
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
    private Object mLock = new Object();
    protected Messenger mRemoteMsgService = null;
    private int mServiceState = 0;

    public GripSensor(Context context) {
        mContext = context;
    }

    public void onStart() {
    if (DEBUG) Log.d(TAG, "Enabling");
    doBindServiceLocked();
    doBindServiceLockedIms();
        Slog.d(TAG, "GripSensor onStart()");
    }

    void disable() {
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

    private void doBindServiceLockedIms() {
        Intent intent = new Intent();
        intent.setAction(REMOTE_BROADCAST_INTENT_ACTION);
        intent.setPackage(REMOTE_BROADCAST_INTENT_CATEGORY);
        try {
            mContext.bindServiceAsUser(intent, mConnection, 1, UserHandle.CURRENT);
            mServiceState = 1;
            Slog.d(TAG, "Bind ASUS keyEvent service");
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}