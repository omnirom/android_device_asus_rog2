package org.omnirom.device.gripsensor;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.hardware.input.InputManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.util.Xml;
import android.view.Display;
import android.view.KeyEvent;
import android.view.WindowManager;
import org.omnirom.device.R;
import org.omnirom.device.Utils;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import vendor.ims.airtrigger.V1_0.SqueezeConfig;
import vendor.ims.airtrigger.V1_0.TabConfig;
import vendor.ims.airtrigger.V1_1.IAirTrigger;

public class AirTriggerUtils {
    private static int DT_DOCK_NAVIGATION_BAR_HEIGHT = 0;
    private static final String[] FIELD_ALLOW_SCREEN_OFF = {"air_trigger_allow_screen_off_short", "air_trigger_allow_screen_off_long"};
    private static int[] mSqueezeValues;
    private static int[] mTapValues;
    static AirTriggerUtils sInstance;
    private Context mContext;
    private Handler mHandler;
    private HandlerThread mThread = new HandlerThread("AirTriggerUtils.Loader");

    public static final String GRIP_FORCE_PATH = "/proc/driver/grip_squeeze_force";
    public static final String GRIP_TAP_FORCE_PATH = "/proc/driver/grip_tap1_force";
    public static final String GRIP_TAP2_FORCE_PATH = "/proc/driver/grip_tap2_force";

    public static AirTriggerUtils getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new AirTriggerUtils(context);
        }
        return sInstance;
    }

    public AirTriggerUtils(Context context) {
        Log.d("AirTriggerUtils", "AirTriggerUtils init");
        this.mContext = context;
        this.mThread.start();
        this.mHandler = new Handler(this.mThread.getLooper());
    }

    public void onDestroy() {
        Log.d("AirTriggerUtils", "AirTriggerUtils onDestroy");
        this.mHandler = null;
        this.mThread.quit();
        sInstance = null;
    }

    public boolean isGripHardwareSupport() {
        String readProp = readProp("persist.asus.hardware.gripsensor", 0);
        Log.d("AirTriggerUtils", "isGripHardwareSupport = " + readProp);
        return readProp.equals("1");
    }

    public void setTapStengthPage(boolean z) {
        setRawDataEnable(z);
        if (!z) {
            setTapEnable(1, z);
            setTapEnable(2, z);
        }
    }

    public void setGripStengthPage(boolean z) {
        setRawDataEnable(z);
    }

    public void setSetupWizardPage(boolean z) {
        if (z) {
            setMainSwitchEnable(z);
        }
        setSqueezeEnable(z);
    }

    public void setGameSpaceEnable(boolean z) {
        Log.d("AirTriggerUtils", "setGameSpaceEnable = " + z);
        setSettingsProviderForGrip(this.mContext.getContentResolver(), "air_trigger_game_center", z ? 1 : 0);
        setSqueeze2Enable(z);
    }

    public static boolean isGameSpaceEnable(Context context) {
        return getSettingsProviderForGrip(context.getContentResolver(), "air_trigger_game_center", 0) == 1;
    }

    public static boolean isMainSwitchEnable(Context context) {
        return getSettingsProviderForGrip(context.getContentResolver(), "air_trigger_enable", 0) == 1;
    }

    public void setMainSwitchEnable(boolean z) {
        Log.d("AirTriggerUtils", "setMainSwitchEnable enable =" + z);
        if (isSupportHidl(1)) {
            setIAirTrigger2Enable(0, true);
        } else if (isSupportHidl(0)) {
            setIAirTriggerEnable(0, z);
        }
        setSqueezeEnable(z);
        if (z) {
            initDefaultValue();
        }
        setSettingsProviderForGrip(this.mContext.getContentResolver(), "air_trigger_enable", z ? 1 : 0);
    }

    public static boolean isAllowScreenOff(Context context, int i) {
        return getSettingsProviderForGrip(context.getContentResolver(), FIELD_ALLOW_SCREEN_OFF[i], 1) == 1;
    }

    public static void setAllowScreenOff(Context context, int i, boolean z) {
        Log.d("AirTriggerUtils", "setAllowScreenOff type= " + i + "enable =" + z);
        setSettingsProviderForGrip(context.getContentResolver(), FIELD_ALLOW_SCREEN_OFF[i], z ? 1 : 0);
    }

    public static boolean isSupportHidl(int i) {
        if (i == 0) {
            try {
                IAirTrigger.getService();
            } catch (RemoteException | NoSuchElementException e) {
                Log.d("AirTriggerUtils", "Exception: " + e);
                return false;
            }
        } else if (i == 1) {
            try {
                vendor.ims.airtrigger.V1_0.IAirTrigger.getService();
            } catch (RemoteException | NoSuchElementException e2) {
                Log.d("AirTriggerUtils", "Exception: " + e2);
                return false;
            }
        }
        return true;
    }

    private void setIAirTriggerEnable(int i, boolean z) {
        mHandler.post(() -> {
            try {
                vendor.ims.airtrigger.V1_0.IAirTrigger.getService().setEnable(i, z);
                Log.d("AirTriggerUtils", "setIAirTriggerEnable mode=" + i + " enable=" + z);
            } catch (RemoteException e) {
                Log.d("AirTriggerUtils", "RemoteException: " + e);
            }
        });
    }

    private void updateTapConfig(TabConfig tabConfig) {
        mHandler.post(() -> {
            try {
                vendor.ims.airtrigger.V1_0.IAirTrigger.getService().setBarTabConfig(tabConfig);
                Log.d("AirTriggerUtils", "updateTapConfig id=" + tabConfig.id + " enable=" + tabConfig.enable + " dur=" + tabConfig.duration + " thr=" + tabConfig.threshold + " fup=" + tabConfig.fup_threshold);
            } catch (RemoteException e) {
                Log.d("AirTriggerUtils", "RemoteException: " + e);
            }
        });
    }

    private void updateSqueezeConfig(SqueezeConfig squeezeConfig) {
        mHandler.post(() -> {
            try {
                vendor.ims.airtrigger.V1_0.IAirTrigger.getService().setBarSqueezeConfig(squeezeConfig);
                Log.d("AirTriggerUtils", " updateSqueezeConfig enable=" + squeezeConfig.enable + " thr=" + squeezeConfig.threshold + " short_limit=" + squeezeConfig.short_limit + " short_duration=" + squeezeConfig.short_duration + " long_duration=" + squeezeConfig.long_duration + " drop_rate=" + squeezeConfig.drop_rate + " drop_total=" + squeezeConfig.drop_total + " up_rate=" + squeezeConfig.up_rate + " up_total=" + squeezeConfig.up_total);
            } catch (RemoteException e) {
                Log.d("AirTriggerUtils", "RemoteException: " + e);
            }
        });
    }

    private void setIAirTrigger2Enable(int i, boolean z) {
        mHandler.post(() -> {
            try {
                IAirTrigger.getService().setEnable(i, z);
                Log.d("AirTriggerUtils", "setIAirTrigger2Enable mode=" + i + " enable=" + z);
            } catch (RemoteException e) {
                Log.d("AirTriggerUtils", "RemoteException: " + e);
            }
        });
    }

    private void updateTapConfig_1(vendor.ims.airtrigger.V1_1.TabConfig tabConfig) {
        mHandler.post(() -> {
            try {
                IAirTrigger.getService().setBarTabConfig_1_1(tabConfig);
                Log.d("AirTriggerUtils", "updateTapConfig_1 id=" + tabConfig.id + " enable=" + tabConfig.enable + " thr=" + tabConfig.threshold + " min_position=" + tabConfig.min_position + " max_position=" + tabConfig.max_position + " delta_release_force=" + tabConfig.delta_release_force + " delta_tap_force=" + tabConfig.delta_tap_force + " slope_window=" + tabConfig.slope_window + " slope_release_force=" + tabConfig.slope_release_force + " slope_tap_force=" + tabConfig.slope_tap_force + " enable_vibration=" + tabConfig.enable_vibration + " vibration_intensity=" + tabConfig.vibration_intensity);
            } catch (RemoteException e) {
                Log.d("AirTriggerUtils", "RemoteException: " + e);
            }
        });
    }

    private void updateSqueezeConfig_1(vendor.ims.airtrigger.V1_1.SqueezeConfig squeezeConfig) {
        mHandler.post(() -> {
            try {
                IAirTrigger.getService().setBarSqueezeConfig_1_1(squeezeConfig);
                Log.d("AirTriggerUtils", " updateSqueezeConfig_1 enable=" + squeezeConfig.enable + " thr=" + squeezeConfig.threshold + " short_duration=" + squeezeConfig.short_duration + " long_duration=" + squeezeConfig.long_duration + " drop_rate=" + squeezeConfig.drop_rate + " drop_total=" + squeezeConfig.drop_total + " up_rate=" + squeezeConfig.up_rate + " up_total=" + squeezeConfig.up_total);
            } catch (RemoteException e) {
                Log.d("AirTriggerUtils", "RemoteException: " + e);
            }
        });
    }

    private void setSqueeze2Enable(boolean z) {
        if (isSupportHidl(1)) {
            vendor.ims.airtrigger.V1_1.SqueezeConfig squeezeConfig = new vendor.ims.airtrigger.V1_1.SqueezeConfig();
            squeezeConfig.id = 2;
            if (z) {
                squeezeConfig.enable = 1;
            } else {
                squeezeConfig.enable = 0;
            }
            squeezeConfig.threshold = -2;
            squeezeConfig.short_duration = -2;
            squeezeConfig.long_duration = -2;
            squeezeConfig.drop_rate = -2;
            squeezeConfig.drop_total = -2;
            squeezeConfig.up_rate = -2;
            squeezeConfig.up_total = -2;
            updateSqueezeConfig_1(squeezeConfig);
        }
        Log.d("AirTriggerUtils", "setSqueeze2Enable enable=: " + z);
    }

    public void initDefaultValue() {
        Log.d("AirTriggerUtils", "initDefaultValue");
        try {
            if (isSupportHidl(1)) {
                IAirTrigger.getService();
                vendor.ims.airtrigger.V1_1.SqueezeConfig squeezeConfig = new vendor.ims.airtrigger.V1_1.SqueezeConfig();
                squeezeConfig.id = 1;
                squeezeConfig.enable = -2;
                squeezeConfig.threshold = -2;
                squeezeConfig.short_duration = 100;
                squeezeConfig.long_duration = 600;
                squeezeConfig.drop_rate = 255;
                squeezeConfig.drop_total = 255;
                squeezeConfig.up_rate = -2;
                squeezeConfig.up_total = -2;
                updateSqueezeConfig_1(squeezeConfig);
                vendor.ims.airtrigger.V1_1.SqueezeConfig squeezeConfig2 = new vendor.ims.airtrigger.V1_1.SqueezeConfig();
                squeezeConfig2.id = 2;
                squeezeConfig2.enable = -2;
                squeezeConfig2.threshold = 60;
                squeezeConfig2.short_duration = 0;
                squeezeConfig2.long_duration = 600;
                squeezeConfig2.drop_rate = 255;
                squeezeConfig2.drop_total = 255;
                squeezeConfig2.up_rate = -2;
                squeezeConfig2.up_total = -2;
                updateSqueezeConfig_1(squeezeConfig2);
                Utils.writeValue("/proc/driver/grip_squeeze_short_dur", "100");
                Utils.writeValue("/proc/driver/grip_squeeze_long_dur", "600");
                Utils.writeValue("/proc/driver/grip_squeeze_up_rate", "255");
                Utils.writeValue("/proc/driver/grip_squeeze_drop_rate", "255");

                //Utils.writeValue("/proc/driver/grip_squeeze1_short_dur", "100");
                Utils.writeValue("/proc/driver/grip_squeeze1_long_dur", "600");
                Utils.writeValue("/proc/driver/grip_squeeze1_up_rate", "255");
                Utils.writeValue("/proc/driver/grip_squeeze1_drop_rate", "255");
            } else if (isSupportHidl(0)) {
                vendor.ims.airtrigger.V1_0.IAirTrigger.getService();
                SqueezeConfig squeezeConfig3 = new SqueezeConfig();
                squeezeConfig3.enable = -2;
                squeezeConfig3.threshold = -2;
                squeezeConfig3.short_limit = 600;
                squeezeConfig3.short_duration = 100;
                squeezeConfig3.long_duration = 600;
                squeezeConfig3.drop_rate = 255;
                squeezeConfig3.drop_total = 255;
                squeezeConfig3.up_rate = -2;
                squeezeConfig3.up_total = -2;
                updateSqueezeConfig(squeezeConfig3);
            }
        } catch (RemoteException e) {
            Log.d("AirTriggerUtils", "RemoteException: " + e);
        }
        setSqueezeThreshold(getSqueezeThresholdLevel());
        setTapThreshold(getTapThresholdLevel(1), 1);
        setTapThreshold(getTapThresholdLevel(2), 2);
        setIncreaseSensitivity(isIncreaseSensitivity());
        setSettingsProviderForGrip(this.mContext.getContentResolver(), "asus_grip_min_raw_pressure_sendout", String.valueOf(10));
    }

    public void setIncreaseSensitivity(boolean z) {
        setSettingsProviderForGrip(this.mContext.getContentResolver(), "air_trigger_sensitivity_booster_enable", z ? 1 : 0);
        if (isSupportHidl(1)) {
            setIAirTrigger2Enable(1, z);
        } else if (isSupportHidl(0)) {
            setIAirTriggerEnable(1, z);
        }
    }

    public boolean isIncreaseSensitivity() {
        if (getSettingsProviderForGrip(this.mContext.getContentResolver(), "air_trigger_sensitivity_booster_enable", 0) == 1) {
            return true;
        }
        return false;
    }

    public void setRawDataEnable(boolean z) {
        if (isSupportHidl(1)) {
            setIAirTrigger2Enable(2, z);
        } else if (isSupportHidl(0)) {
            setIAirTriggerEnable(2, z);
        }
    }

    public void setSqueezeThreshold(int i) {
        setSettingsProviderForGrip(this.mContext.getContentResolver(), "air_trigger_squeeze_threshold_level", i);
        if (isSupportHidl(1)) {
            vendor.ims.airtrigger.V1_1.SqueezeConfig squeezeConfig = new vendor.ims.airtrigger.V1_1.SqueezeConfig();
            squeezeConfig.id = 1;
            squeezeConfig.threshold = getSqueezeValue(i);
            squeezeConfig.enable = -2;
            squeezeConfig.short_duration = -2;
            squeezeConfig.long_duration = -2;
            squeezeConfig.drop_rate = -2;
            squeezeConfig.drop_total = -2;
            squeezeConfig.up_rate = -2;
            squeezeConfig.up_total = -2;
            updateSqueezeConfig_1(squeezeConfig);
            Utils.writeLine(GRIP_FORCE_PATH, String.valueOf(getSqueezeValue(i)));
        } else if (isSupportHidl(0)) {
            SqueezeConfig squeezeConfig2 = new SqueezeConfig();
            squeezeConfig2.threshold = getSqueezeValue(i);
            squeezeConfig2.enable = -2;
            squeezeConfig2.short_limit = -2;
            squeezeConfig2.short_duration = -2;
            squeezeConfig2.long_duration = -2;
            squeezeConfig2.drop_rate = -2;
            squeezeConfig2.drop_total = -2;
            squeezeConfig2.up_rate = -2;
            squeezeConfig2.up_total = -2;
            updateSqueezeConfig(squeezeConfig2);
        }
        Log.d("AirTriggerUtils", "setSqueezeThreshold=" + i);
    }

    public int getSqueezeValue(int i) {
        return getSqueezeArray()[i - 1];
    }

    public int[] getSqueezeArray() {
        String[] strArr;
        if (mSqueezeValues == null) {
            String[] valuesFromXml = getValuesFromXml();
            if (valuesFromXml.length != 0) {
                strArr = valuesFromXml[0].split(",");
            } else {
                strArr = this.mContext.getResources().getStringArray(R.array.grip_squeeze_values);
            }
            mSqueezeValues = new int[strArr.length];
            Log.d("AirTriggerUtils", "getSqueezeArray=" + Arrays.toString(strArr));
            for (int i = 0; i < strArr.length; i++) {
                mSqueezeValues[i] = Integer.parseInt(strArr[i]);
            }
        }
        return mSqueezeValues;
    }

    public int getSqueezeThresholdLevel() {
        return getSettingsProviderForGrip(this.mContext.getContentResolver(), "air_trigger_squeeze_threshold_level", 5);
    }

    public void setSqueezeEnable(boolean z) {
        int i = 1;
        if (isSupportHidl(1)) {
            vendor.ims.airtrigger.V1_1.SqueezeConfig squeezeConfig = new vendor.ims.airtrigger.V1_1.SqueezeConfig();
            squeezeConfig.id = 1;
             if (z) {
                squeezeConfig.enable = 1;
            } else {
                squeezeConfig.enable = 0;
            }
            squeezeConfig.threshold = -2;
            squeezeConfig.short_duration = -2;
            squeezeConfig.long_duration = -2;
            squeezeConfig.drop_rate = -2;
            squeezeConfig.drop_total = -2;
            squeezeConfig.up_rate = -2;
            squeezeConfig.up_total = -2;
            updateSqueezeConfig_1(squeezeConfig);
            vendor.ims.airtrigger.V1_1.SqueezeConfig squeezeConfig2 = new vendor.ims.airtrigger.V1_1.SqueezeConfig();
            squeezeConfig2.id = 2;
            if (!z || !isGameSpaceEnable(this.mContext)) {
                i = 0;
            }
            squeezeConfig2.enable = i;
            squeezeConfig2.threshold = -2;
            squeezeConfig2.short_duration = -2;
            squeezeConfig2.long_duration = -2;
            squeezeConfig2.drop_rate = -2;
            squeezeConfig2.drop_total = -2;
            squeezeConfig2.up_rate = -2;
            squeezeConfig2.up_total = -2;
            updateSqueezeConfig_1(squeezeConfig2);
        } else if (isSupportHidl(0)) {
            SqueezeConfig squeezeConfig3 = new SqueezeConfig();
             if (z) {
                squeezeConfig3.enable = 1;
            } else {
                squeezeConfig3.enable = 0;
            }
            squeezeConfig3.threshold = -2;
            squeezeConfig3.short_limit = -2;
            squeezeConfig3.short_duration = -2;
            squeezeConfig3.long_duration = -2;
            squeezeConfig3.drop_rate = -2;
            squeezeConfig3.drop_total = -2;
            squeezeConfig3.up_rate = -2;
            squeezeConfig3.up_total = -2;
            updateSqueezeConfig(squeezeConfig3);
        }
        Log.d("AirTriggerUtils", "setSqueezeEnable enable=" + z);
    }

    public void setTapThreshold(int i, int i2) {
        int i3 = 1;
        setSettingsProviderForGrip(this.mContext.getContentResolver(), i2 == 1 ? "air_trigger_tap_left_threshold_level" : "air_trigger_tap_right_threshold_level", i);
        int i4 = 2;
        if (isSupportHidl(1)) {
            vendor.ims.airtrigger.V1_1.TabConfig tabConfig = new vendor.ims.airtrigger.V1_1.TabConfig();
            if (i2 == 1) {
                i4 = 1;
            }
            tabConfig.id = i4;
            tabConfig.enable = -2;
            tabConfig.threshold = getTapValue(i);
            tabConfig.min_position = -2;
            tabConfig.max_position = -2;
            tabConfig.delta_release_force = -2;
            tabConfig.delta_tap_force = -2;
            tabConfig.slope_window = -2;
            tabConfig.slope_release_force = -2;
            tabConfig.slope_tap_force = -2;
            tabConfig.enable_vibration = 1;
            tabConfig.vibration_intensity = -2;
            updateTapConfig_1(tabConfig);
            if (i2 ==1) {
                Utils.writeLine(GRIP_TAP_FORCE_PATH, String.valueOf(getTapValue(i)));
            } else {
                Utils.writeLine(GRIP_TAP2_FORCE_PATH, String.valueOf(getTapValue(i)));
            }
        } else if (isSupportHidl(0)) {
            TabConfig tabConfig2 = new TabConfig();
            if (i2 != 1) {
                i3 = 2;
            }
            tabConfig2.id = i3;
            tabConfig2.enable = -2;
            tabConfig2.threshold = getTapValue(i);
            tabConfig2.duration = -2;
            tabConfig2.fup_threshold = -2;
            updateTapConfig(tabConfig2);
        }
        Log.d("AirTriggerUtils", "setTapThreshold int=" + i + " int2=" + i2);
    }

    public int getTapValue(int i) {
        return getTapArray()[i - 1];
    }

    public int[] getTapArray() {
        String[] strArr;
        if (mTapValues == null) {
            String[] valuesFromXml = getValuesFromXml();
            if (valuesFromXml.length != 0) {
                strArr = valuesFromXml[1].split(",");
            } else {
                strArr = this.mContext.getResources().getStringArray(R.array.tap_threshold_values);
            }
            mTapValues = new int[strArr.length];
            Log.d("AirTriggerUtils", "getTapArray=" + Arrays.toString(strArr));
            for (int i = 0; i < strArr.length; i++) {
                mTapValues[i] = Integer.parseInt(strArr[i]);
            }
        }
        return mTapValues;
    }

    public void setTapEnable(int i, boolean z) {
        int i2 = 1;
        int i3 = 2;
        if (isSupportHidl(1)) {
            vendor.ims.airtrigger.V1_1.TabConfig tabConfig = new vendor.ims.airtrigger.V1_1.TabConfig();
            if (i == 1) {
                i3 = 1;
            }
            tabConfig.id = i3;
            tabConfig.enable = z ? 1 : 0;
            tabConfig.threshold = -2;
            tabConfig.min_position = -2;
            tabConfig.max_position = -2;
            tabConfig.delta_release_force = -2;
            tabConfig.delta_tap_force = -2;
            tabConfig.slope_window = -2;
            tabConfig.slope_release_force = -2;
            tabConfig.slope_tap_force = -2;
            tabConfig.enable_vibration = 1;
            tabConfig.vibration_intensity = -2;
            updateTapConfig_1(tabConfig);
        } else if (isSupportHidl(0)) {
            TabConfig tabConfig2 = new TabConfig();
            if (i != 1) {
                i2 = 2;
            }
            tabConfig2.id = i2;
             if (z) {
                tabConfig2.enable = 1;
            } else {
                tabConfig2.enable = 0;
            }
            tabConfig2.threshold = -2;
            tabConfig2.duration = -2;
            tabConfig2.fup_threshold = -2;
            updateTapConfig(tabConfig2);
        }
        Log.d("AirTriggerUtils", "setTapEnable int=" + i + " enable=" + z);
    }

    public int getTapThresholdLevel(int i) {
        return getSettingsProviderForGrip(this.mContext.getContentResolver(), i == 1 ? "air_trigger_tap_left_threshold_level" : "air_trigger_tap_right_threshold_level", 4);
    }

    public static int getAppUID(Context context, String str) {
        try {
            return context.getPackageManager().getApplicationInfo(str, 0).uid;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static void injectKeycode(int i) {
        long uptimeMillis = SystemClock.uptimeMillis();
        InputManager.getInstance().injectInputEvent(new KeyEvent(uptimeMillis, uptimeMillis, 0, i, 0, 0, -1, 0, 0, 257), 0);
        InputManager.getInstance().injectInputEvent(new KeyEvent(uptimeMillis, uptimeMillis, 1, i, 0, 0, -1, 0, 0, 257), 0);
    }

    private String readProp(String str, int i) {
        return SystemProperties.get(str, String.valueOf(i));
    }

    public static void setSettingsProviderForGrip(ContentResolver contentResolver, String str, String str2) {
        Settings.System.putString(contentResolver, str, str2);
        Log.d("AirTriggerUtils", "set SettingsProvider: " + str + " , put String =" + str2);
    }

    public static void setSettingsProviderForGrip(ContentResolver contentResolver, String str, int i) {
        Settings.Global.putInt(contentResolver, str, i);
        Log.d("AirTriggerUtils", "set SettingsProvider: " + str + " , put Int =" + i);
    }

    public static int getSettingsProviderForGrip(ContentResolver contentResolver, String str, int i) {
        int i2 = Settings.Global.getInt(contentResolver, str, i);
        Log.d("AirTriggerUtils", "get SettingsProvider: " + str + " , get Int =" + i2);
        return i2;
    }

    public static String getSettingsProviderForGrip(ContentResolver contentResolver, String str, String str2) {
        String string = Settings.System.getString(contentResolver, str);
        Log.d("AirTriggerUtils", "get SettingsProvider: " + str);
        if (string != null) {
            Log.d("AirTriggerUtils", " , get String =" + string);
            return string;
        }
        Log.d("AirTriggerUtils", " , get String =" + str2);
        return str2;
    }

    public static int getDtDockNavigationBarHeight(Context context) {
        int i = DT_DOCK_NAVIGATION_BAR_HEIGHT;
        if (i > 0) {
            return i;
        }
        int identifier = context.getResources().getIdentifier("navigation_bar_height_dt_dock_mode", "dimen", "android");
        if (identifier > 0) {
            DT_DOCK_NAVIGATION_BAR_HEIGHT = context.getResources().getDimensionPixelSize(identifier);
        }
        return DT_DOCK_NAVIGATION_BAR_HEIGHT;
    }

    public static int getCurrentDisplayId(Context context) {
        return getCurrentDisplay(context).getDisplayId();
    }

    public static boolean isTwinViewModeEnabled(Context context) {
        getCurrentDisplayId(context);
        return getCurrentDisplayId(context) != 0;
    }

    public static int getDisplayMode(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(), "display_mode_with_dock", 0);
    }

    public static Display getCurrentDisplay(Context context) {
        Display defaultDisplay = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        int dockingType = Utils.dockingType(context);
        if (dockingType != 7 && dockingType != 8) {
            return defaultDisplay;
        }
        if (dockingType == 8 && getDisplayMode(context) == 0) {
            return defaultDisplay;
        }
        Display[] displays = ((DisplayManager) context.getSystemService("display")).getDisplays();
        if (displays.length <= 1) {
            return defaultDisplay;
        }
        Pattern compile = Pattern.compile("uniqueId \"local:\\d+\"");
        for (Display display : displays) {
            if (compile.matcher(display.toString()).find() && display.getDisplayId() != 0) {
                return display;
            }
        }
        return defaultDisplay;
    }

    private String[] getValuesFromXml() {
        String[] strArr = new String[0];
        File file = new File(this.mContext.getExternalFilesDir(null), "Airtrigger_arrays.xml");
        return file.exists() ? readFromXml(file) : strArr;
    }

    private String[] readFromXml(File file) {
        Log.d("AirTriggerUtils", "Reading from " + file.toString());
        String[] strArr = new String[2];
        try {
            XmlPullParser newPullParser = Xml.newPullParser();
            FileReader fileReader = new FileReader(file);
            newPullParser.setInput(fileReader);
            for (int eventType = newPullParser.getEventType(); eventType != 1; eventType = newPullParser.next()) {
                if (eventType == 2) {
                    if (newPullParser.getName().equals("grip")) {
                        strArr[0] = newPullParser.nextText();
                    } else if (newPullParser.getName().equals("tap")) {
                        strArr[1] = newPullParser.nextText();
                    }
                }
            }
            fileReader.close();
        } catch (IOException | XmlPullParserException e) {
            Log.w("AirTriggerUtils", "Problem reading ", e);
        }
        return strArr;
    }
}
