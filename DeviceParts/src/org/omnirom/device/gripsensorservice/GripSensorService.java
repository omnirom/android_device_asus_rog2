package org.omnirom.device.gripsensorservice;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.UserHandle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;
import androidx.core.app.JobIntentService;
import org.omnirom.device.R;
import org.omnirom.device.gripsensor.AirTriggerUtils;
import org.omnirom.device.gripsensor.GripContentObserver;
import org.omnirom.device.gripsensor.GripUtils;
import org.omnirom.device.gripsensor.SqueezeFragment;

public class GripSensorService extends JobIntentService {
    private static boolean isGamingFlag = false;
    private static boolean isSettingFlag = false;
    private static boolean isTapSettingPageFlag = false;
    private static boolean mCanVibrate = false;
    private static GripBgHandler mGripBgHandler = null;
    private static boolean mIsAllSqueezeClosed = true;
    private static boolean mIsRelease = false;
    private static boolean mIsSqueeze = false;
    private static boolean mLongCam = false;
    private static boolean mLongGrip = true;
    private static boolean mLongGripLocked = true;
    private static boolean mShortGrip = true;
    private static boolean mShortGripLocked = true;
    private static int mShowTapSide = 0;
    private static boolean mStartTap = false;
    private AudioAttributes mAudioAttrs = new AudioAttributes.Builder().setUsage(4).build();
    private GripSensorFloatingView mFloatingView;
    private GripBroadcastReceiver mGripBroadcastReceiver;
    private GripContentObserver mGripContentObserver;
    private HandlerThread mGripThread;
    private GripUIHandler mGripUIHandler;
    private KeyguardManager mKeyguardManager;
    private int mLastLeftPressure;
    private int mLastRightPressure;
    private int mMaxPressure = 0;
    private int mMaxTapPressure = 0;
    private Messenger mMessenger;
    private PowerManager mPowerManager;
    private BroadcastReceiver mSettingsPageReceiver;
    private int mTapPressure;
    private GripSensorTapView mTapView;
    private Vibrator mVibrator;

    public static final int GRIP_ID = 1000;

    public static void setCanVibrate(boolean z) {
        mCanVibrate = z;
    }

    public static void setIsAllSqueezeClosed(boolean z) {
        mIsAllSqueezeClosed = z;
    }

    class GripUIHandler extends Handler {
        GripUIHandler() {
        }

        public void handleMessage(Message message) {
            int i = message.what;
            if (i != 7002) {
                switch (i) {
                    case 6000:
                        Log.d("AsusSettingsGripSensorService", "MSG_UPDATE_GRIP_KEYCODE_INFO");
                        KeyEvent keyEvent = new KeyEvent((KeyEvent) message.obj);
                        int keyCode = keyEvent.getKeyCode();
                        int metaState = keyEvent.getMetaState();
                        int action = keyEvent.getAction();
                        if (keyCode == 866) {
                            Log.d("AsusSettingsGripSensorService", "KEYCODE_GRIP_AVAILABLE_SQUEEZE");
                            boolean unused = GripSensorService.mIsRelease = true;
                            return;
                        } else if (keyCode != 878) {
                            switch (keyCode) {
                                case 843:
                                    Log.d("AsusSettingsGripSensorService", "<KEYCODE_GRIP_L1_TAP>");
                                    if (GripSensorService.isSettingFlag && GripSensorService.isTapSettingPageFlag && GripSensorService.mShowTapSide != 2) {
                                        GripSensorService.this.showTapView(1, action);
                                        return;
                                    }
                                    return;
                                case 844:
                                    Log.d("AsusSettingsGripSensorService", "<KEYCODE_GRIP_R1_TAP>");
                                    if (GripSensorService.isSettingFlag && GripSensorService.isTapSettingPageFlag && GripSensorService.mShowTapSide != 1) {
                                        GripSensorService.this.showTapView(2, action);
                                        return;
                                    }
                                    return;
                                case 845:
                                    Log.d("AsusSettingsGripSensorService", "<KEYCODE_GRIP_R2_TAP>");
                                    return;
                                case 846:
                                    Log.d("AsusSettingsGripSensorService", "<KEYCODE_GRIP_SHORT_SQUEEZE> action=" + action);
                                    if (!GripBgHandler.isAllowedHandleSqueezeAction()) {
                                        Log.d("AsusSettingsGripSensorService", "<KEYCODE_GRIP_SHORT_SQUEEZE> Not allowed.");
                                        return;
                                    } else if (!GripSensorService.isGamingFlag && !GripSensorService.isTapSettingPageFlag && action == 1) {
                                        if (GripSensorService.isSettingFlag && GripSensorService.mCanVibrate) {
                                            GripSensorService.this.asusVibrator(10007);
                                            GripSensorService.this.notifyActivityStartAnim();
                                        }
                                        if (!GripSensorService.isSettingFlag) {
                                            if (GripSensorService.this.isSqueezeEnabled(846) && GripSensorService.this.isVibrateEnable()) {
                                                GripSensorService.this.asusVibrator(10008);
                                            }
                                            GripSensorService.this.hideFloatingView();
                                            GripSensorService.this.setRawData(false);
                                            return;
                                        }
                                        return;
                                    } else {
                                        return;
                                    }
                                case 847:
                                    Log.d("AsusSettingsGripSensorService", "<KEYCODE_GRIP_LONG_SQUEEZE> action=" + action);
                                    if (!GripBgHandler.isAllowedHandleSqueezeAction()) {
                                        Log.d("AsusSettingsGripSensorService", "<KEYCODE_GRIP_LONG_SQUEEZE> Not allowed.");
                                        return;
                                    } else if (!GripSensorService.isGamingFlag && !GripSensorService.isTapSettingPageFlag && action == 1) {
                                        if (GripSensorService.isSettingFlag && GripSensorService.mCanVibrate) {
                                            GripSensorService.this.asusVibrator(10007);
                                            GripSensorService.this.notifyActivityStartAnim();
                                        }
                                        if (!GripSensorService.isSettingFlag) {
                                            if (GripSensorService.this.isSqueezeEnabled(847) && GripSensorService.this.isVibrateEnable()) {
                                                boolean unused2 = GripSensorService.mIsRelease = false;
                                                GripSensorService.this.sendReleaseNotify();
                                                GripSensorService.this.asusVibrator(10008);
                                            }
                                            GripSensorService.this.hideFloatingView();
                                            GripSensorService.this.setRawData(false);
                                            return;
                                        }
                                        return;
                                    } else {
                                        return;
                                    }
                                case 848:
                                    Log.d("AsusSettingsGripSensorService", "<KEYCODE_GRIP_L1_RAW_DATA>");
                                    if (GripSensorService.isSettingFlag && GripSensorService.isTapSettingPageFlag && GripSensorService.mShowTapSide != 2) {
                                        int unused3 = GripSensorService.this.mTapPressure = metaState;
                                        GripSensorService.this.showTapView(1, 3);
                                        return;
                                    }
                                    return;
                                case 849:
                                    Log.d("AsusSettingsGripSensorService", "<KEYCODE_GRIP_R1_RAW_DATA>");
                                    if (GripSensorService.isGamingFlag || GripSensorService.isTapSettingPageFlag || !GripSensorService.mIsSqueeze) {
                                        if (GripSensorService.isSettingFlag && GripSensorService.isTapSettingPageFlag && GripSensorService.mShowTapSide != 1) {
                                            int unused4 = GripSensorService.this.mTapPressure = metaState;
                                            GripSensorService.this.showTapView(2, 3);
                                            return;
                                        }
                                        return;
                                    } else if (GripSensorService.isSettingFlag || GripSensorService.this.isSqueezeEnabled(854) || !GripSensorService.this.isVibrateEnable()) {
                                        int unused5 = GripSensorService.this.mLastRightPressure = metaState;
                                        GripSensorService.this.showFloatingView();
                                        return;
                                    } else {
                                        return;
                                    }
                                case 850:
                                    Log.d("AsusSettingsGripSensorService", "<KEYCODE_GRIP_" + keyCode + "_RAW_DATA> pressure: " + metaState);
                                    if (!GripSensorService.isGamingFlag && !GripSensorService.isTapSettingPageFlag && GripSensorService.mIsSqueeze) {
                                        if (GripSensorService.isSettingFlag || GripSensorService.this.isSqueezeEnabled(854) || !GripSensorService.this.isVibrateEnable()) {
                                            int unused6 = GripSensorService.this.mLastLeftPressure = metaState;
                                            return;
                                        }
                                        return;
                                    }
                                    return;
                                case 851:
                                    Log.d("AsusSettingsGripSensorService", "<KEYCODE_GRIP_GLOBAL_MODE>");
                                    boolean unused7 = GripSensorService.isSettingFlag = false;
                                    boolean unused8 = GripSensorService.isGamingFlag = false;
                                    GripSensorService.this.hideFloatingView();
                                    GripSensorService.this.hideTapView();
                                    return;
                                case 852:
                                    Log.d("AsusSettingsGripSensorService", "<KEYCODE_GRIP_GAME_MODE>");
                                    GripSensorService.this.hideFloatingView();
                                    boolean unused9 = GripSensorService.isSettingFlag = false;
                                    boolean unused10 = GripSensorService.isGamingFlag = true;
                                    GripSensorService.this.hideTapView();
                                    return;
                                case 853:
                                    Log.d("AsusSettingsGripSensorService", "<KEYCODE_GRIP_SETTING_MODE>");
                                    boolean unused11 = GripSensorService.isSettingFlag = true;
                                    boolean unused12 = GripSensorService.isGamingFlag = false;
                                    GripSensorService.this.hideTapView();
                                    return;
                                case 854:
                                    Log.d("AsusSettingsGripSensorService", "<KEYCODE_GRIP_START_SQUEEZE> action=" + action);
                                    if (!GripBgHandler.isAllowedHandleSqueezeAction()) {
                                        Log.d("AsusSettingsGripSensorService", "<KEYCODE_GRIP_START_SQUEEZE> Not allowed.");
                                        return;
                                    } else if (!GripSensorService.isGamingFlag && !GripSensorService.isTapSettingPageFlag && action == 0) {
                                        if (GripSensorService.isSettingFlag) {
                                            GripSensorService.this.notifyActivityHideHint();
                                        } else if (GripSensorService.this.isSqueezeEnabled(854) && GripSensorService.this.isVibrateEnable()) {
                                            GripSensorService.this.asusVibrator(10007);
                                        }
                                        GripSensorService.this.setRawData(true);
                                        return;
                                    } else {
                                        return;
                                    }
                                case 855:
                                    Log.d("AsusSettingsGripSensorService", "<KEYCODE_GRIP_CANCEL_SQUEEZE> action=" + action);
                                    if (!GripSensorService.isGamingFlag && !GripSensorService.isTapSettingPageFlag && action == 1 && !GripSensorService.isSettingFlag) {
                                        GripSensorService.this.hideFloatingView();
                                        GripSensorService.this.setRawData(false);
                                        return;
                                    }
                                    return;
                                default:
                                    return;
                            }
                        } else {
                            Log.d("AsusSettingsGripSensorService", "<KEYCODE_GRIP_GAME_MODE_DISABLE_GESTURE>");
                            GripSensorService.this.hideFloatingView();
                            boolean unused13 = GripSensorService.isSettingFlag = false;
                            boolean unused14 = GripSensorService.isGamingFlag = true;
                            GripSensorService.this.hideTapView();
                            return;
                        }
                    case 6001:
                        Bundle data = message.getData();
                        if (data != null) {
                            String string = data.getString("audio_api");
                            String string2 = data.getString("audio_caller");
                            Log.d("AsusSettingsGripSensorService", "<MSG_AUDIO_OUTPUT_CHANGED_EVENT> api: " + string + " ; caller: " + string2);
                            if (GripSensorService.mGripBgHandler == null) {
                                return;
                            }
                            if ("setSpeakerphoneOn".equals(string) || "setBluetoothScoOn".equals(string)) {
                                GripSensorService.mGripBgHandler.removeMessages(6001);
                                GripSensorService.mGripBgHandler.sendMessageDelayed(GripSensorService.mGripBgHandler.obtainMessage(6001), 200);
                                return;
                            }
                            return;
                        }
                        return;
                    case 6002:
                        float[] floatArray = message.getData().getFloatArray("data");
                        int i2 = (int) floatArray[0];
                        if (i2 == 0) {
                            int i3 = (int) floatArray[2];
                            int i4 = (int) floatArray[3];
                            if (i3 == 0) {
                                Log.d("AsusSettingsGripSensorService", "<RAW_LEFT> pressure=" + i4);
                                if (GripSensorService.isSettingFlag && GripSensorService.isTapSettingPageFlag && GripSensorService.mShowTapSide != 2) {
                                    int unused15 = GripSensorService.this.mTapPressure = i4;
                                    GripSensorService.this.showTapView(1, 3);
                                    return;
                                }
                                return;
                            }
                            Log.d("AsusSettingsGripSensorService", "<RAW_RIGHT> pressure=" + i4);
                            if (GripSensorService.isGamingFlag || GripSensorService.isTapSettingPageFlag || !GripSensorService.mIsSqueeze) {
                                if (GripSensorService.isSettingFlag && GripSensorService.isTapSettingPageFlag && GripSensorService.mShowTapSide != 1) {
                                    int unused16 = GripSensorService.this.mTapPressure = i4;
                                    GripSensorService.this.showTapView(2, 3);
                                    return;
                                }
                                return;
                            } else if (GripSensorService.isSettingFlag || GripSensorService.this.isSqueezeEnabled(854) || !GripSensorService.this.isVibrateEnable()) {
                                int unused17 = GripSensorService.this.mLastRightPressure = i4;
                                GripSensorService.this.showFloatingView();
                                return;
                            } else {
                                return;
                            }
                        } else if (i2 == 1) {
                            int i5 = (int) floatArray[1];
                            Log.d("AsusSettingsGripSensorService", "<GESTURE_TAP_LEFT> action=" + i5);
                            if (GripSensorService.isSettingFlag && GripSensorService.isTapSettingPageFlag && GripSensorService.mShowTapSide != 2) {
                                GripSensorService.this.showTapView(1, i5);
                                return;
                            }
                            return;
                        } else if (i2 != 2) {
                            switch (i2) {
                                case 5:
                                    Log.d("AsusSettingsGripSensorService", "<GESTURE_SWIPE_LEFT> action=" + ((int) floatArray[1]));
                                    return;
                                case 6:
                                    Log.d("AsusSettingsGripSensorService", "<GESTURE_SWIPE_RIGHT> action=" + ((int) floatArray[1]));
                                    return;
                                case 7:
                                    int i6 = (int) floatArray[1];
                                    Log.d("AsusSettingsGripSensorService", "<GESTURE_SHORT_SQUEEZE> action=" + i6);
                                    if (!GripBgHandler.isAllowedHandleSqueezeAction()) {
                                        Log.d("AsusSettingsGripSensorService", "<GESTURE_SHORT_SQUEEZE> Not allowed.");
                                        return;
                                    } else if (i6 == 0 && !GripSensorService.isGamingFlag && !GripSensorService.isTapSettingPageFlag) {
                                        if (GripSensorService.isSettingFlag && GripSensorService.mCanVibrate) {
                                            GripSensorService.this.asusVibrator(10007);
                                            GripSensorService.this.notifyActivityStartAnim();
                                        }
                                        if (!GripSensorService.isSettingFlag) {
                                            if (GripSensorService.this.isSqueezeEnabled(846) && GripSensorService.this.isVibrateEnable()) {
                                                GripSensorService.this.asusVibrator(10008);
                                            }
                                            GripSensorService.this.hideFloatingView();
                                            GripSensorService.this.setRawData(false);
                                            return;
                                        }
                                        return;
                                    } else {
                                        return;
                                    }
                                case 8:
                                    int i7 = (int) floatArray[1];
                                    Log.d("AsusSettingsGripSensorService", "<GESTURE_LONG_SQUEEZE> action=" + i7);
                                    if (!GripBgHandler.isAllowedHandleSqueezeAction()) {
                                        Log.d("AsusSettingsGripSensorService", "<GESTURE_LONG_SQUEEZE> Not allowed.");
                                        return;
                                    } else if (i7 == 0 && !GripSensorService.isGamingFlag && !GripSensorService.isTapSettingPageFlag) {
                                        if (GripSensorService.isSettingFlag && GripSensorService.mCanVibrate) {
                                            GripSensorService.this.asusVibrator(10007);
                                            GripSensorService.this.notifyActivityStartAnim();
                                        }
                                        if (!GripSensorService.isSettingFlag) {
                                            if (GripSensorService.this.isSqueezeEnabled(847) && GripSensorService.this.isVibrateEnable()) {
                                                boolean unused18 = GripSensorService.mIsRelease = false;
                                                GripSensorService.this.sendReleaseNotify();
                                                GripSensorService.this.asusVibrator(10008);
                                            }
                                            GripSensorService.this.hideFloatingView();
                                            GripSensorService.this.setRawData(false);
                                            return;
                                        }
                                        return;
                                    } else {
                                        return;
                                    }
                                case 9:
                                    int i8 = (int) floatArray[1];
                                    Log.d("AsusSettingsGripSensorService", "<GESTURE_START_SQUEEZE> action=" + i8);
                                    if (!GripBgHandler.isAllowedHandleSqueezeAction()) {
                                        Log.d("AsusSettingsGripSensorService", "<GESTURE_START_SQUEEZE> Not allowed.");
                                        return;
                                    } else if (i8 == 0 && !GripSensorService.isGamingFlag && !GripSensorService.isTapSettingPageFlag) {
                                        if (GripSensorService.isSettingFlag) {
                                            GripSensorService.this.notifyActivityHideHint();
                                        } else if (GripSensorService.this.isSqueezeEnabled(854) && GripSensorService.this.isVibrateEnable()) {
                                            GripSensorService.this.asusVibrator(10007);
                                        }
                                        GripSensorService.this.setRawData(true);
                                        return;
                                    } else {
                                        return;
                                    }
                                case 10:
                                    int i9 = (int) floatArray[1];
                                    Log.d("AsusSettingsGripSensorService", "<GESTURE_CANCEL_SQUEEZE> action=" + i9);
                                    if (i9 == 0 && !GripSensorService.isGamingFlag && !GripSensorService.isTapSettingPageFlag && !GripSensorService.isSettingFlag) {
                                        GripSensorService.this.hideFloatingView();
                                        GripSensorService.this.setRawData(false);
                                        return;
                                    }
                                    return;
                                case 11:
                                    int i10 = (int) floatArray[1];
                                    Log.d("AsusSettingsGripSensorService", "<GESTURE_END_SQUEEZE> action=" + i10);
                                    if (i10 == 0) {
                                        boolean unused19 = GripSensorService.mIsRelease = true;
                                        return;
                                    }
                                    return;
                                case 12:
                                    float f = floatArray[2];
                                    float f2 = floatArray[5];
                                    Log.d("AsusSettingsGripSensorService", "<GESTURE_SLIDE_LEFT> pressure=" + ((int) floatArray[3]) + " center=" + f2);
                                    return;
                                case 13:
                                    float f3 = floatArray[2];
                                    float f4 = floatArray[5];
                                    Log.d("AsusSettingsGripSensorService", "<GESTURE_SLIDE_RIGHT> pressure=" + ((int) floatArray[3]) + " center=" + f4);
                                    return;
                                default:
                                    return;
                            }
                        } else {
                            int i11 = (int) floatArray[1];
                            Log.d("AsusSettingsGripSensorService", "<GESTURE_TAP_RIGHT> action=" + i11);
                            if (GripSensorService.isSettingFlag && GripSensorService.isTapSettingPageFlag && GripSensorService.mShowTapSide != 1) {
                                GripSensorService.this.showTapView(2, i11);
                                return;
                            }
                            return;
                        }
                    case 6003:
                        Log.d("AsusSettingsGripSensorService", "<MSG_UPDATE_OPEN_DND_DIALOG>");
                        GripSensorService.this.showPromptDialog();
                        return;
                    default:
                        super.handleMessage(message);
                        return;
                }
            } else if (!GripSensorService.mIsRelease) {
                Toast.makeText(GripSensorService.this.getApplicationContext(), GripSensorService.this.getResources().getString(R.string.grip_notifi_release), 0).show();
            }
        }
    }

    private void showPromptDialog() {
    }

    private void setRawData(boolean z) {
        mIsSqueeze = z;
        AirTriggerUtils.getInstance(getApplication()).setRawDataEnable(z);
    }

    private void initAirtriggerUtils() {
        AirTriggerUtils.getInstance(getApplication()).setMainSwitchEnable(AirTriggerUtils.isMainSwitchEnable(getApplication()));
    }

    private String getForegroundActivity() {
        String className = ((ActivityManager) getSystemService("activity")).getRunningTasks(1).get(0).topActivity.getClassName();
        Log.d("AsusSettingsGripSensorService", "CURRENT Activity ::" + className);
        return className;
    }

    private boolean isVibrateEnable() {
        if (!mLongCam || !getForegroundActivity().equals("com.asus.camera.CameraApp")) {
            Log.d("AsusSettingsGripSensorService", "isVibrateEnable = true");
            return true;
        }
        Log.d("AsusSettingsGripSensorService", "isVibrateEnable = false");
        return false;
    }

    public static void setSwitchState(int i, boolean z) {
        if (i == 1) {
            mShortGripLocked = z;
        } else if (i == 2) {
            mLongGripLocked = z;
        } else if (i == 3) {
            mShortGrip = z;
        } else if (i == 4) {
            mLongGrip = z;
        } else if (i == 5) {
            mLongCam = z;
        }
    }

    private boolean isLockSqueezeEnable(int i) {
        if (i != 0) {
            if (i != 1) {
                return true;
            }
            if (AirTriggerUtils.isSupportHidl(1)) {
                return mLongGripLocked && AirTriggerUtils.isAllowScreenOff(getApplicationContext(), 1);
            }
            return mLongGripLocked;
        } else if (AirTriggerUtils.isSupportHidl(1)) {
            return mShortGripLocked && AirTriggerUtils.isAllowScreenOff(getApplicationContext(), 0);
        } else {
            return mShortGripLocked;
        }
    }

    private boolean isSqueezeEnabled(int i) {
        boolean inKeyguardRestrictedInputMode = this.mKeyguardManager.inKeyguardRestrictedInputMode();
        boolean z = true;
        boolean z2 = this.mPowerManager.isScreenOn() && !inKeyguardRestrictedInputMode;
        if (i == 846 ? (!z2 || !mShortGrip) && (z2 || !isLockSqueezeEnable(0)) : i == 847 ? (!z2 || !mLongGrip) && (z2 || !isLockSqueezeEnable(1)) : i != 854 || ((!z2 || (!mLongGrip && !mShortGrip)) && (z2 || (!isLockSqueezeEnable(0) && !isLockSqueezeEnable(1))))) {
            z = false;
        }
        Log.d("AsusSettingsGripSensorService", "ScreenLock = " + inKeyguardRestrictedInputMode + " Screen On && Unlock = " + z2 + " isSqueezeEnabled = " + z);
        return z;
    }

    private void sendReleaseNotify() {
        GripUIHandler gripUIHandler = this.mGripUIHandler;
        if (gripUIHandler != null) {
            gripUIHandler.removeMessages(7002);
            this.mGripUIHandler.sendMessageDelayed(this.mGripUIHandler.obtainMessage(7002), 1300);
        }
    }

    private void registGripSwitchObserver() {
        this.mGripContentObserver = new GripContentObserver(mGripBgHandler);
        getContentResolver().registerContentObserver(GripUtils.URI_SHORT_GRIP, true, this.mGripContentObserver);
        getContentResolver().registerContentObserver(GripUtils.URI_LONG_GRIP, true, this.mGripContentObserver);
        getContentResolver().registerContentObserver(GripUtils.URI_TAP, true, this.mGripContentObserver);
        initGripSwitch();
    }

    private void initGripSwitch() {
        GripBgHandler gripBgHandler = mGripBgHandler;
        if (gripBgHandler != null) {
            gripBgHandler.setContext(getBaseContext());
            Message obtainMessage = mGripBgHandler.obtainMessage();
            obtainMessage.what = 5004;
            mGripBgHandler.sendMessage(obtainMessage);
        }
    }

    public static void enqueueWork(Context context, Intent intent) {
        Log.d("AsusSettingsGripSensorService", "enqueueWork " + intent);
        enqueueWork(context, GripSensorService.class, GRIP_ID, intent);
    }

    @Override
    protected void onHandleWork(Intent intent) {
        Log.d("AsusSettingsGripSensorService", "onHandleWork: " + intent.getStringExtra("work").toString());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("AsusSettingsGripSensorService", "onCreate");
        initHandler();
        this.mKeyguardManager = (KeyguardManager) getSystemService("keyguard");
        this.mPowerManager = (PowerManager) getSystemService("power");
        this.mVibrator = (Vibrator) getSystemService("vibrator");
        this.mMessenger = new Messenger(this.mGripUIHandler);
        registGripSwitchObserver();
        initAirtriggerUtils();
        initFloatingView();
        registerGripReceiver();
        initGlobalSetting();
        checkLanguage();
        initSwitchState();
    }

    private void initSwitchState() {
        mShortGripLocked = SqueezeFragment.isSwitchEnabled(getContentResolver(), 1);
        mLongGripLocked = SqueezeFragment.isSwitchEnabled(getContentResolver(), 2);
        mShortGrip = SqueezeFragment.isSwitchEnabled(getContentResolver(), 3);
        mLongGrip = SqueezeFragment.isSwitchEnabled(getContentResolver(), 4);
        Log.d("AsusSettingsGripSensorService", "initSwitchState() mShortGripLocked = " + mShortGripLocked + " mLongGripLocked = " + mLongGripLocked + " mShortGrip = " + mShortGrip + " mLongGrip = " + mLongGrip);
    }

    private void initHandler() {
        this.mGripUIHandler = new GripUIHandler();
        this.mGripThread = new HandlerThread("GripBgHandler");
        this.mGripThread.start();
        mGripBgHandler = new GripBgHandler(this.mGripThread.getLooper());
    }

    public static void initGlobalSetting() {
        Log.d("AsusSettingsGripSensorService", "initGlobalSetting");
        GripBgHandler gripBgHandler = mGripBgHandler;
        if (gripBgHandler != null) {
            gripBgHandler.removeMessages(7003);
            mGripBgHandler.sendMessageDelayed(mGripBgHandler.obtainMessage(7003), 7003);
        }
    }

    public IBinder onBind(Intent intent) {
        Log.d("AsusSettingsGripSensorService", "onBind");
        return this.mMessenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int i, int i2) {
        Log.d("AsusSettingsGripSensorService", "onStartCommand");
        return super.onStartCommand(intent, i, i2);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("AsusSettingsGripSensorService", "onDestroy");
        releaseFloatingView();
        releaseTapView();
        this.mGripThread.quit();
        AirTriggerUtils.getInstance(getApplication()).onDestroy();
    }

    private void initFloatingView() {
        if (!Settings.canDrawOverlays(getBaseContext())) {
            Log.d("AsusSettingsGripSensorService", "can not DrawOverlays");
            return;
        }
        this.mFloatingView = new GripSensorFloatingView(this);
        this.mTapView = new GripSensorTapView(this);
        hideFloatingView();
        hideTapView();
    }

    private void showFloatingView() {
        int i = this.mLastRightPressure;
        if (this.mMaxPressure < i) {
            this.mMaxPressure = i;
        }
        Log.d("AsusSettingsGripSensorService", "showFloatingView averagePressure = " + i + " mMaxPressure = " + this.mMaxPressure);
        GripSensorFloatingView gripSensorFloatingView = this.mFloatingView;
        if (gripSensorFloatingView != null) {
            gripSensorFloatingView.show(1, i);
            this.mFloatingView.show(2, i);
        }
        int i2 = this.mMaxPressure;
        if (i2 >= 65 && isSettingFlag) {
            updateGripState(i2);
        }
        if (i == 0) {
            this.mMaxPressure = 0;
            hideFloatingView();
        }
    }

    private void hideFloatingView() {
        Log.d("AsusSettingsGripSensorService", "hideFloatingView");
        GripSensorFloatingView gripSensorFloatingView = this.mFloatingView;
        if (gripSensorFloatingView != null) {
            gripSensorFloatingView.hide();
        }
    }

    private void releaseFloatingView() {
        Log.d("AsusSettingsGripSensorService", "releaseFloatingView");
        GripSensorFloatingView gripSensorFloatingView = this.mFloatingView;
        if (gripSensorFloatingView != null) {
            gripSensorFloatingView.release();
        }
    }

    private void releaseTapView() {
        Log.d("AsusSettingsGripSensorService", "releaseTapView");
        GripSensorTapView gripSensorTapView = this.mTapView;
        if (gripSensorTapView != null) {
            gripSensorTapView.release();
        }
    }

    private void showTapView(int i, int i2) {
        int i3;
        Log.d("AsusSettingsGripSensorService", "showTapView action=" + i2 + " mTapPressure=" + this.mTapPressure + " mMaxTapPressure=" + this.mMaxTapPressure);
        GripSensorTapView gripSensorTapView = this.mTapView;
        if (gripSensorTapView == null) {
            return;
        }
        if (i2 == 0) {
            mStartTap = true;
            this.mMaxTapPressure = this.mTapPressure;
            gripSensorTapView.show(i);
            updateTapState(this.mMaxTapPressure, i);
            if (mCanVibrate) {
                notifyActivityStartTapAnim(i);
            }
        } else if (i2 == 1) {
            mStartTap = false;
            gripSensorTapView.hide(i);
        } else if (mStartTap && this.mMaxTapPressure < (i3 = this.mTapPressure)) {
            this.mMaxTapPressure = i3;
            updateTapState(this.mMaxTapPressure, i);
        }
    }

    private void hideTapView() {
        Log.d("AsusSettingsGripSensorService", "hideTapView");
        GripSensorTapView gripSensorTapView = this.mTapView;
        if (gripSensorTapView != null) {
            gripSensorTapView.hide(1);
            this.mTapView.hide(2);
            this.mTapView.hide();
        }
    }

    private void asusVibrator(int i) {
        VibrationEffect vibrationEffect;
        Log.d("AsusSettingsGripSensorService", "Enter AsusVibrator");
        //if (getBaseContext().getPackageManager().hasSystemFeature("asus.hardware.touchsense")) {
            //vibrationEffect = VibrationEffect.createOneShot(0, i);
        //} else {
            vibrationEffect = VibrationEffect.get(2);
        //}
        Vibrator vibrator = this.mVibrator;
        if (vibrator != null) {
            vibrator.vibrate(vibrationEffect, this.mAudioAttrs);
        }
    }

    private void updateTapState(int i, int i2) {
        Log.d("AsusSettingsGripSensorService", "updateTapState pressure=" + i + " side=" + i2);
        notifyTapActivityUpdateUI(i, i2);
    }

    private void updateGripState(int i) {
        Log.d("AsusSettingsGripSensorService", "updateGripState");
        notifyActivityUpdateUI(i);
    }

    private void notifyActivityStartAnim() {
        Log.d("AsusSettingsGripSensorService", "notify GripStengthAdustActivity to start animation");
        Intent intent = new Intent();
        intent.setAction("org.omnirom.device.NOTIFY_GRIP_ANIMATE");
        intent.setPackage("org.omnirom.device");
        sendBroadcastAsUser(intent, UserHandle.CURRENT);
    }

    private void notifyActivityStartTapAnim(int i) {
        Log.d("AsusSettingsGripSensorService", "notify GripStengthAdustActivity to start tap animation");
        Intent intent = new Intent();
        intent.setAction("org.omnirom.device.NOTIFY_TAP_ANIMATE");
        intent.setPackage("org.omnirom.device");
        intent.putExtra("tap_side", i);
        sendBroadcastAsUser(intent, UserHandle.CURRENT);
    }

    private void notifyActivityUpdateUI(int i) {
        Log.d("AsusSettingsGripSensorService", "notify GripStengthAdust/Test Activity to Update UI");
        Intent intent = new Intent();
        intent.setAction("org.omnirom.device.NOTIFY_GRIP_UI_UPDATE");
        intent.setPackage("org.omnirom.device");
        intent.putExtra("grip_press", i);
        sendBroadcastAsUser(intent, UserHandle.CURRENT);
    }

    private void notifyActivityHideHint() {
        Log.d("AsusSettingsGripSensorService", "notify GripStengthAdust/Test Activity to hide hint position");
        Intent intent = new Intent();
        intent.setAction("org.omnirom.device.NOTIFY_GRIP_ACTIVITY_HIDE_HINT_POSITION");
        intent.setPackage("org.omnirom.device");
        sendBroadcastAsUser(intent, UserHandle.CURRENT);
    }

    private void notifyTapActivityUpdateUI(int i, int i2) {
        Log.d("AsusSettingsGripSensorService", "notify TapStengthAdust/Test Activity to Update UI");
        Intent intent = new Intent();
        intent.setAction("org.omnirom.device.NOTIFY_TAP_UI_UPDATE");
        intent.setPackage("org.omnirom.device");
        intent.putExtra("tap_press", i);
        intent.putExtra("tap_side", i2);
        sendBroadcastAsUser(intent, UserHandle.CURRENT);
    }

    private void checkLanguage() {
        mGripBgHandler.obtainMessage(7005).sendToTarget();
    }

    private void registerGripReceiver() {
        if (this.mGripBroadcastReceiver == null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.SCREEN_OFF");
            intentFilter.addAction("android.intent.action.SCREEN_ON");
            intentFilter.addAction("android.intent.action.USER_PRESENT");
            intentFilter.addAction("android.intent.action.HEADSET_PLUG");
            intentFilter.addAction("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED");
            intentFilter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
            intentFilter.addAction("org.omnirom.device.NOTIFY_AIRTRIGGER_LONG_GRIP_ON");
            intentFilter.addAction("android.intent.action.LOCALE_CHANGED");
            intentFilter.addAction("adjust_vibration");
            this.mGripBroadcastReceiver = new GripBroadcastReceiver(mGripBgHandler);
            getApplicationContext().registerReceiver(this.mGripBroadcastReceiver, intentFilter);
        }
        if (this.mSettingsPageReceiver == null) {
            IntentFilter intentFilter2 = new IntentFilter();
            intentFilter2.addAction("org.omnirom.device.NOTIFY_TAP_SETTING_PAGE");
            intentFilter2.addAction("org.omnirom.device.NOTIFY_LEAVE_TAP_SETTING_PAGE");
            intentFilter2.addAction("org.omnirom.device.NOTIFY_GRIP_SETTING_PAGE");
            intentFilter2.addAction("org.omnirom.device.NOTIFY_LEAVE_GRIP_SETTING_PAGE");
            intentFilter2.addAction("org.omnirom.device.NOTIFY_TAP_SIDE");
            this.mSettingsPageReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (action.equals("org.omnirom.device.NOTIFY_TAP_SETTING_PAGE")) {
                        Log.d("AsusSettingsGripSensorService", "onReceive NOTIFY_TAP_SETTING_PAGE");
                        boolean unused = GripSensorService.isTapSettingPageFlag = true;
                        GripSensorService.this.hideFloatingView();
                    } else if (action.equals("org.omnirom.device.NOTIFY_LEAVE_TAP_SETTING_PAGE")) {
                        Log.d("AsusSettingsGripSensorService", "onReceive NOTIFY_LEAVE_TAP_SETTING_PAGE");
                        boolean unused2 = GripSensorService.isTapSettingPageFlag = false;
                        GripSensorService.this.hideTapView();
                    } else if (action.equals("org.omnirom.device.NOTIFY_TAP_SIDE")) {
                        int intExtra = intent.getIntExtra("tap_side", 1);
                        Log.d("AsusSettingsGripSensorService", "onReceive NOTIFY_TAP_SIDE " + intExtra);
                        int unused3 = GripSensorService.mShowTapSide = intExtra;
                        if (intExtra == 1) {
                            if (GripSensorService.this.mTapView != null) {
                                GripSensorService.this.mTapView.hide(2);
                            }
                        } else if (intExtra == 2 && GripSensorService.this.mTapView != null) {
                            GripSensorService.this.mTapView.hide(1);
                        }
                    } else if (action.equals("org.omnirom.device.NOTIFY_GRIP_SETTING_PAGE")) {
                        Log.d("AsusSettingsGripSensorService", "onReceive NOTIFY_GRIP_SETTING_PAGE");
                        boolean unused4 = GripSensorService.isSettingFlag = true;
                        GripSensorService.mGripBgHandler.obtainMessage(8000).sendToTarget();
                    } else if (action.equals("org.omnirom.device.NOTIFY_LEAVE_GRIP_SETTING_PAGE")) {
                        Log.d("AsusSettingsGripSensorService", "onReceive NOTIFY_LEAVE_GRIP_SETTING_PAGE");
                        boolean unused5 = GripSensorService.isSettingFlag = false;
                    }
                }
            };
            getApplicationContext().registerReceiver(this.mSettingsPageReceiver, intentFilter2);
        }
    }
}
