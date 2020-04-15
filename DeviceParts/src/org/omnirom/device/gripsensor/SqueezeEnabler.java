package org.omnirom.device.gripsensor;

import android.content.ContentResolver;
import android.content.Context;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

public class SqueezeEnabler {
    private final Context mContext;
    private final Preference mPreference;
    private ContentResolver mResolver;
    private int mSqueezeType;

    public void pause() {
    }

    public SqueezeEnabler(Context context, Preference preference, int i) {
        this.mContext = context;
        this.mPreference = preference;
        this.mSqueezeType = i;
        this.mResolver = context.getContentResolver();
    }

    public void resume() {
        handleStateChanged(getSensorState());
    }

    private boolean getSensorState() {
        if (!AirTriggerUtils.isMainSwitchEnable(this.mContext)) {
            return false;
        }
        int i = this.mSqueezeType;
        if (i == 5) {
            return AirTriggerUtils.isGameSpaceEnable(this.mContext);
        }
        if (i == 6) {
            return AirTriggerUtils.isMainSwitchEnable(this.mContext);
        }
        if (!this.mPreference.getKey().equals(AllowScreenOffPreferenceController.KEY)) {
            return SqueezeFragment.isSwitchEnabled(this.mResolver, this.mSqueezeType);
        }
        if (!SqueezeFragment.isSwitchEnabled(this.mResolver, this.mSqueezeType - 2) || !SqueezeFragment.isSwitchEnabled(this.mResolver, this.mSqueezeType) || !AirTriggerUtils.isAllowScreenOff(this.mContext, this.mSqueezeType - 3)) {
            return false;
        }
        return true;
    }

    private void handleStateChanged(boolean z) {
        Preference preference = this.mPreference;
            if (preference instanceof SwitchPreference) {
                ((SwitchPreference) preference).setChecked(z);
            if (this.mPreference.getKey().equals(AllowScreenOffPreferenceController.KEY)) {
                ((SwitchPreference) this.mPreference).setEnabled(AirTriggerUtils.isMainSwitchEnable(this.mContext) && SqueezeFragment.isSwitchEnabled(this.mResolver, this.mSqueezeType) && SqueezeFragment.isSwitchEnabled(this.mResolver, this.mSqueezeType + -2));
            } else {
                ((SwitchPreference) this.mPreference).setEnabled(AirTriggerUtils.isMainSwitchEnable(this.mContext));
            }
        }
    }
}
