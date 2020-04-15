package org.omnirom.device.gripsensorservice;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.hardware.SensorEvent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import org.omnirom.device.Utils;
import org.omnirom.device.gripsensor.AirTriggerUtils;
import org.omnirom.device.gripsensor.GripUtils;
import org.omnirom.device.gripsensor.SqueezeFragment;

public class GripBgHandler extends Handler {
    private static boolean mIsAllowAirtrigger = true;
    private static int mPSensorState = -1;
    private static int mPhoneCallState;
    private AudioManager mAudioManager;
    private Context mContext;
    private GripUtils.GripData mGlobalLongCam = null;
    private GripUtils.GripData mGlobalLongGrip = null;
    private GripUtils.GripData mGlobalLongGripLocked = null;
    private GripUtils.GripData mGlobalShortGrip = null;
    private GripUtils.GripData mGlobalShortGripLocked = null;
    private GripPhoneStateListener mPhoneStateListener;

    private void onTabTableChanged() {
    }

    public void setContext(Context context) {
        if (this.mContext == null) {
            this.mContext = context;
        }
        initPhoneSensor();
    }

    public GripBgHandler(Looper looper) {
        super(looper);
    }

    public void handleMessage(Message message) {
        super.handleMessage(message);
        int i = message.what;
        int i2 = 0;
        if (i == 5004) {
            Log.d("GripBgHandler", " Handle MSG_GRIP_TABLE_CHANGED_EVENT " + message.arg1);
            int i3 = message.arg1;
            if (i3 == 0) {
                onShortTableChanged();
            } else if (i3 == 1) {
                onLongTableChanged();
            } else if (i3 == 2) {
                onTabTableChanged();
            }
            refreshGripConfig(0);
            GripSensorService.setIsAllSqueezeClosed(isAllSqueezeFuncClosed());
        } else if (i == 6001) {
            Log.d("GripBgHandler", "Handle MSG_AUDIO_OUTPUT_CHANGED_EVENT");
            controlPSensorListener();
        } else if (i == 7000) {
            Log.d("GripBgHandler", "Handle MSG_SENSOR_STATE_CHANGED_EVENT");
            Object obj = message.obj;
            if (obj == null || ((SensorEvent) obj).sensor == null) {
                Log.d("GripBgHandler", "<MSG_SENSOR_STATE_CHANGED_EVENT> Get invalid sensor event.");
                return;
            }
            SensorEvent sensorEvent = (SensorEvent) obj;
            if (sensorEvent.sensor.getType() == 8) {
                float[] fArr = sensorEvent.values;
                if (fArr.length != 0) {
                    if (((int) fArr[0]) == 0) {
                        i2 = 1;
                    }
                    mPSensorState = i2;
                    Log.d("GripBgHandler", "<MSG_SENSOR_STATE_CHANGED_EVENT> PSensor state: " + mPSensorState);
                }
            }
        } else if (i == 7001) {
            Log.d("GripBgHandler", "Handle MSG_PHONE_STATE_CHANGED_EVENT Calling state: " + message.arg1);
            mPhoneCallState = message.arg1;
            controlPSensorListener();
        } else if (i != 8000 && i != 8001) {
            switch (i) {
                case 7003:
                    Log.d("GripBgHandler", "Handle MSG_INIT_SWITCH_STATE_EVENT");
                    initSwitchState();
                    return;
                case 7004:
                    Log.d("GripBgHandler", "Handle MSG_SETUP_WIZRAD_EVENT");
                    SqueezeFragment.setSwitchState(this.mContext, 4, true);
                    SqueezeFragment.setSwitchState(this.mContext, 2, true);
                    return;
                case 7005:
                    Log.d("GripBgHandler", "Handle MSG_LANGUAGE_CHANGED_EVENT");
                    checkLanguage();
                    return;
                default:
                    return;
            }
        }
    }

    private void checkLanguage() {
        if (!Utils.isCNSKU()) {
            for (int i = 1; i <= 4; i++) {
                SqueezeFragment.getSwitchLabel(this.mContext, i, 1);
            }
        }
    }

    private boolean hasInit(ContentResolver contentResolver, Uri uri, int i) {
        Cursor query = contentResolver.query(ContentUris.withAppendedId(uri, (long) i), null, null, null, null);
        if (query != null && query.getCount() > 0) {
            return true;
        }
        if (query == null) {
            return false;
        }
        query.close();
        return false;
    }

    private void initSwitchState() {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        if (!hasInit(contentResolver, GripUtils.URI_SHORT_GRIP, 2)) {
            if (Utils.isCNSKU()) {
                GripUtils.addOrUpdateGripData(GripUtils.URI_SHORT_GRIP, contentResolver, 2, "Global_grip", -1, null, -1, null, null, -1, -1, -1, null, null, -1, null, null, 4);
                GripUtils.addOrUpdateGripData(GripUtils.URI_SHORT_GRIP, contentResolver, 1, "Global_grip_locked", -1, null, -1, null, null, -1, -1, -1, null, null, -1, null, null, 4);
            } else {
                GripUtils.addOrUpdateGripData(GripUtils.URI_SHORT_GRIP, contentResolver, 2, "Global_grip", -1, null, -1, null, null, -1, -1, 219, null, null, -1, null, null, -1);
                GripUtils.addOrUpdateGripData(GripUtils.URI_SHORT_GRIP, contentResolver, 1, "Global_grip_locked", -1, null, -1, null, null, -1, -1, 219, null, null, -1, null, null, -1);
            }
            GripUtils.addOrUpdateGripData(GripUtils.URI_LONG_GRIP, contentResolver, 2, "Global_grip", -1, null, -1, null, null, -1, -1, -1, null, null, -1, null, null, 2);
            GripUtils.addOrUpdateGripData(GripUtils.URI_LONG_GRIP, contentResolver, 1, "Global_grip_locked", -1, null, -1, null, null, -1, -1, -1, null, null, -1, null, null, 2);
            if (Utils.isCNSKU()) {
                AirTriggerUtils.setSettingsProviderForGrip(contentResolver, "asus_grip_short_squeeze", String.valueOf(6));
                AirTriggerUtils.setSettingsProviderForGrip(contentResolver, "asus_grip_locked_short_squeeze", String.valueOf(6));
            } else {
                AirTriggerUtils.setSettingsProviderForGrip(contentResolver, "asus_grip_short_squeeze", String.valueOf(4));
                AirTriggerUtils.setSettingsProviderForGrip(contentResolver, "asus_grip_locked_short_squeeze", String.valueOf(4));
            }
            AirTriggerUtils.setSettingsProviderForGrip(contentResolver, "asus_grip_long_squeeze", String.valueOf(6));
            AirTriggerUtils.setSettingsProviderForGrip(contentResolver, "asus_grip_locked_long_squeeze", String.valueOf(6));
        }
    }

    private void onShortTableChanged() {
        this.mGlobalShortGrip = null;
        this.mGlobalShortGripLocked = null;
    }

    private void onLongTableChanged() {
        this.mGlobalLongGrip = null;
        this.mGlobalLongGripLocked = null;
        this.mGlobalLongCam = null;
    }

    private void refreshGripConfig(int i) {
        if (this.mGlobalShortGripLocked == null) {
            this.mGlobalShortGripLocked = readShortGripData(1, this.mContext);
        }
        if (this.mGlobalLongGripLocked == null) {
            this.mGlobalLongGripLocked = readLongGripData(1, this.mContext);
        }
        if (this.mGlobalLongCam == null) {
            this.mGlobalLongCam = readLongGripData(AirTriggerUtils.getAppUID(this.mContext, "com.asus.camera"), this.mContext);
        }
        if (this.mGlobalShortGrip == null) {
            this.mGlobalShortGrip = readShortGripData(2, this.mContext);
        }
        if (this.mGlobalLongGrip == null) {
            this.mGlobalLongGrip = readLongGripData(2, this.mContext);
        }
    }

    private GripUtils.GripData readShortGripData(int i, Context context) {
        return GripUtils.readGripData(GripUtils.URI_SHORT_GRIP, context.getContentResolver(), i);
    }

    private GripUtils.GripData readLongGripData(int i, Context context) {
        return GripUtils.readGripData(GripUtils.URI_LONG_GRIP, context.getContentResolver(), i);
    }

    private boolean isAllSqueezeFuncClosed() {
        GripUtils.GripData gripData = this.mGlobalShortGripLocked;
        int i = gripData != null ? gripData.mCurrentMode : -1;
        GripUtils.GripData gripData2 = this.mGlobalLongGripLocked;
        int i2 = gripData2 != null ? gripData2.mCurrentMode : -1;
        GripUtils.GripData gripData3 = this.mGlobalShortGrip;
        int i3 = gripData3 != null ? gripData3.mCurrentMode : -1;
        GripUtils.GripData gripData4 = this.mGlobalLongGrip;
        int i4 = gripData4 != null ? gripData4.mCurrentMode : -1;
        GripUtils.GripData gripData5 = this.mGlobalLongCam;
        int i5 = gripData5 != null ? gripData5.mCurrentMode : -1;
        GripSensorService.setSwitchState(1, i != -1);
        GripSensorService.setSwitchState(2, i2 != -1);
        GripSensorService.setSwitchState(3, i3 != -1);
        GripSensorService.setSwitchState(4, i4 != -1);
        GripSensorService.setSwitchState(5, i5 != -1);
        return i == -1 && i2 == -1 && i3 == -1 && i4 == -1;
    }

    private void initPhoneSensor() {
        if (this.mPhoneStateListener == null) {
            this.mPhoneStateListener = new GripPhoneStateListener(this.mContext, 32, this);
            this.mPhoneStateListener.register();
        }
        if (this.mAudioManager == null) {
            this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        }
    }

    public static boolean isAllowedHandleSqueezeAction() {
        int i = mPhoneCallState;
        boolean z = true;
        if (i == 2) {
            z = mIsAllowAirtrigger;
        } else if (i == 1) {
            z = false;
        }
        Log.d("GripBgHandler", "<isAllowedHandleSqueezeAction> " + z);
        return z;
    }

    private boolean isAllowedAirtrigger() {
        boolean z = false;
        if (this.mAudioManager != null) {
            Log.d("GripBgHandler", "isAllowedAirtrigger: \n  isWiredHeadsetOn: " + this.mAudioManager.isWiredHeadsetOn() + "\n  isSpeakerphoneOn: " + this.mAudioManager.isWiredHeadsetOn() + "\n  isBluetoothScoOn: " + this.mAudioManager.isBluetoothScoOn());
            if (this.mAudioManager.isWiredHeadsetOn() || this.mAudioManager.isSpeakerphoneOn() || this.mAudioManager.isBluetoothScoOn()) {
                z = true;
            }
        }
        Log.d("GripBgHandler", "isAllowedAirtrigger: " + z);
        return z;
    }

    private void controlPSensorListener() {
        int i = mPhoneCallState;
        if (i == 0) {
            Log.d("GripBgHandler", "<controlPSensorListener> idle state.");
        } else if (i == 1) {
            Log.d("GripBgHandler", "<controlPSensorListener> Ringing state.");
        } else if (i == 2) {
            Log.d("GripBgHandler", "<controlPSensorListener> Off hook state.");
            mIsAllowAirtrigger = isAllowedAirtrigger();
        }
    }
}
