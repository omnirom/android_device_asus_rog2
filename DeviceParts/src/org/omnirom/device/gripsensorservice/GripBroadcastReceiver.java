package org.omnirom.device.gripsensorservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

public class GripBroadcastReceiver extends BroadcastReceiver {
    private Handler mHandler;

    private String bluetoothAdapterStateToString(int i) {
        switch (i) {
            case 10:
                return "OFF";
            case 11:
                return "TURNING ON";
            case 12:
                return "ON";
            case 13:
                return "TURNING OFF";
            default:
                return "UNKNOWN STATE";
        }
    }

    private String bluetoothConnectionStateToString(int i) {
        return i != 0 ? i != 1 ? i != 2 ? i != 3 ? "UNKNOWN STATE" : "DISCONNECTING" : "CONNECTED" : "CONNECTING" : "DISCONNECTED";
    }

    public GripBroadcastReceiver(Handler handler) {
        this.mHandler = handler;
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent != null ? intent.getAction() : null;
        if (action.equals("android.intent.action.SCREEN_OFF")) {
            Log.d("GripBroadcastReceiver", "onReceive ACTION_SCREEN_OFF");
            this.mHandler.obtainMessage(8001).sendToTarget();
        } else if (action.equals("android.intent.action.SCREEN_ON")) {
            Log.d("GripBroadcastReceiver", "onReceive ACTION_SCREEN_ON");
            this.mHandler.obtainMessage(8000).sendToTarget();
        } else if (action.equals("android.intent.action.USER_PRESENT")) {
            Log.d("GripBroadcastReceiver", "onReceive ACTION_USER_PRESENT");
            this.mHandler.obtainMessage(8000).sendToTarget();
        } else if (action.equals("org.omnirom.device.NOTIFY_AIRTRIGGER_LONG_GRIP_ON")) {
            Log.d("GripBroadcastReceiver", "onReceive NOTIFY_AIRTRIGGER_LONG_GRIP_ON");
            this.mHandler.obtainMessage(7004).sendToTarget();
        } else if (action.equals("adjust_vibration")) {
            boolean booleanExtra = intent.getBooleanExtra("can_vibrate", false);
            Log.d("GripBroadcastReceiver", "onReceive ADJUST_VIBRATION " + booleanExtra);
            GripSensorService.setCanVibrate(booleanExtra);
        } else if ("android.intent.action.HEADSET_PLUG".equals(action)) {
            Log.d("GripBroadcastReceiver", "onReceive ACTION_HEADSET_PLUG");
            this.mHandler.obtainMessage(6001).sendToTarget();
        } else if ("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED".equals(action)) {
            int intExtra = intent.getIntExtra("android.bluetooth.adapter.extra.CONNECTION_STATE", -1);
            Log.d("GripBroadcastReceiver", "onReceive BLUETOOTH_CONNECTION_CHANGE " + bluetoothConnectionStateToString(intExtra));
            if (intExtra == 0 || 2 == intExtra) {
                this.mHandler.obtainMessage(6001).sendToTarget();
            }
        } else if ("android.bluetooth.adapter.action.STATE_CHANGED".equals(action)) {
            int intExtra2 = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", -1);
            Log.d("GripBroadcastReceiver", "onReceive BLUETOOTH_STATE_CHANGE " + bluetoothAdapterStateToString(intExtra2));
            if (10 == intExtra2 || 12 == intExtra2) {
                this.mHandler.obtainMessage(6001).sendToTarget();
            }
        } else if (action.equals("android.intent.action.LOCALE_CHANGED")) {
            Log.d("GripBroadcastReceiver", "onReceive LOCALE_CHANGED");
            this.mHandler.obtainMessage(7005).sendToTarget();
        }
    }
}
