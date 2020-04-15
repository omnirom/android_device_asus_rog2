package org.omnirom.device.gripsensor;

import android.content.Context;

public class AllowScreenOffPreferenceController {
    public static final String KEY = "allow_screen_off";

    public AllowScreenOffPreferenceController(Context context, String KEY, int i) {
    }

    public int getAvailabilityStatus() {
        return AirTriggerUtils.isSupportHidl(1) ? 0 : 3;
    }
}
