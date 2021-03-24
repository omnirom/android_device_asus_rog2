package android.os;

public abstract class VibratorServiceInternal {
    public static final int VIBRATOR_APP_INIT_COMPLETE = 4000;

    public abstract void notify(int i);
}
