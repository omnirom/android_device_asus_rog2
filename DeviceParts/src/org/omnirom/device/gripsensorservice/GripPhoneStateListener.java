package org.omnirom.device.gripsensorservice;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Slog;

public class GripPhoneStateListener extends PhoneStateListener {
    private static final Boolean DEBUG = true;
    private int mEvents;
    private Handler mHandler;
    private TelephonyManager mTelephonyManager;

    public GripPhoneStateListener(Context context, int i, Handler handler) {
        if (context != null && handler != null) {
            this.mHandler = handler;
            this.mEvents = i;
            this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
        }
    }

    public void onCallStateChanged(int i, String str) {
        if (DEBUG.booleanValue()) {
            Slog.d("GripPhoneStateListener", "onCallStateChanged: state: " + i);
        }
        this.mHandler.removeMessages(7001);
        Message obtainMessage = this.mHandler.obtainMessage(7001);
        obtainMessage.arg1 = i;
        this.mHandler.sendMessage(obtainMessage);
    }

    public void register() {
        TelephonyManager telephonyManager = this.mTelephonyManager;
        if (telephonyManager != null) {
            telephonyManager.listen(this, this.mEvents);
        }
    }
}
