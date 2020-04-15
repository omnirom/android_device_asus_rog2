package org.omnirom.device.gripsensor;

import android.content.UriMatcher;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

public class GripContentObserver extends ContentObserver {
    private static final UriMatcher mMatcher = new UriMatcher(-1);
    private Handler mHandler;

    static {
        mMatcher.addURI("org.omnirom.device.gripsensor", "short_grip_table", 1);
        mMatcher.addURI("org.omnirom.device.gripsensor", "long_grip_table", 2);
        mMatcher.addURI("org.omnirom.device.gripsensor", "tap_grip_table", 3);
        mMatcher.addURI("org.omnirom.device.gripsensor", "short_grip_table/#", 4);
        mMatcher.addURI("org.omnirom.device.gripsensor", "long_grip_table/#", 5);
        mMatcher.addURI("org.omnirom.device.gripsensor", "tap_grip_table/#", 6);
    }

    public GripContentObserver(Handler handler) {
        super(handler);
        this.mHandler = handler;
    }

    public void onChange(boolean z, Uri uri) {
        super.onChange(z, uri);
        switch (mMatcher.match(uri)) {
            case 1:
            case 4:
                Handler handler = this.mHandler;
                if (handler != null) {
                    Message obtainMessage = handler.obtainMessage();
                    obtainMessage.what = 5004;
                    obtainMessage.arg1 = 0;
                    this.mHandler.sendMessage(obtainMessage);
                    return;
                }
                return;
            case 2:
            case 5:
                Handler handler2 = this.mHandler;
                if (handler2 != null) {
                    Message obtainMessage2 = handler2.obtainMessage();
                    obtainMessage2.what = 5004;
                    obtainMessage2.arg1 = 1;
                    this.mHandler.sendMessage(obtainMessage2);
                    return;
                }
                return;
            case 3:
            case 6:
                Handler handler3 = this.mHandler;
                if (handler3 != null) {
                    Message obtainMessage3 = handler3.obtainMessage();
                    obtainMessage3.what = 5004;
                    obtainMessage3.arg1 = 2;
                    this.mHandler.sendMessage(obtainMessage3);
                    return;
                }
                return;
            default:
                return;
        }
    }
}
