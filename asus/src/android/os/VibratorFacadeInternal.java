package android.os;

public abstract class VibratorFacadeInternal {
    public static final int AW_EFFECT_VIB = 9;
    public static final int DW_EFFECT_VIB_1ST = 6;
    public static final int DW_EFFECT_VIB_2ND = 7;
    public static final int DW_EFFECT_VIB_ALL = 8;
    public static final int EFFECT_ID_UPPERBOUND = 10;
    public static final int EFFECT_TOUCHSENSE_BASE = 10000;
    public static final int EFFECT_TOUCHSENSE_CATEGORY_COUNT = 4;
    public static final int EFFECT_TOUCHSENSE_GAP = 1000;
    public static final int EFFECT_TOUCHSENSE_ORDER_ALARM = 3;
    public static final int EFFECT_TOUCHSENSE_ORDER_NOTIFICATION = 2;
    public static final int EFFECT_TOUCHSENSE_ORDER_RINGTONE = 1;
    public static final int EFFECT_TOUCHSENSE_ORDER_SYSTEM = 0;
    public static final int IVT_EFFECT_VIB_1ST = 1;
    public static final int IVT_EFFECT_VIB_2ND = 2;
    public static final int IVT_EFFECT_VIB_ALL = 3;
    public static final int POP_UP_FOR_JEDI = 10023;
    public static final int SHUTDOWN_VIBRATOR = 10024;
    public static final int TOUCHSENSE_EFFECT_CLICK = 10045;
    public static final int USAGE_TOUCH_HAPTIC_FEEDBACK = 9528;

    public abstract void init(int i);

    public abstract void playerEvent(int i, int i2, int i3);

    public abstract void playerSampleRate(int i, int i2, int i3, int i4);

    public abstract void playerSessionId(int i, int i2, int i3);

    public abstract void releasePlayer(int i, int i2);

    public abstract void setA2VAlgo(int i);

    public abstract void setA2VAuto(String str, int i, int i2, int i3, int i4);

    public abstract void setA2VEnable(String str, int i, int i2, int i3);

    public abstract void setAmptitude(int i);

    public abstract void setSessionId(String str, int i, int i2, int i3);

    public abstract void trackPlayer(int i, int i2, int i3);

    public abstract void vibrateOff(boolean z);

    public abstract void vibrateOn(VibrateMessage vibrateMessage);
}
