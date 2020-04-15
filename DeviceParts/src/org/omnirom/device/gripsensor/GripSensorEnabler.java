package org.omnirom.device.gripsensor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import androidx.preference.Preference;
import org.omnirom.device.widget.MasterSwitchPreference;

public class GripSensorEnabler implements Preference.OnPreferenceChangeListener {
    private final Context mContext;
    private final IntentFilter mIntentFilter;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            boolean access$000 = GripSensorEnabler.this.getSensorState();
            if (action.equals("org.omnirom.device.SYSTEMUI_AIR_TRIGGER_ON")) {
                access$000 = true;
            } else if (action.equals("org.omnirom.device.SYSTEMUI_AIR_TRIGGER_OFF")) {
                access$000 = false;
            }
            GripSensorEnabler.this.handleStateChanged(access$000);
        }
    };
    private final MasterSwitchPreference mSwitch;

    public GripSensorEnabler(Context context, MasterSwitchPreference masterSwitchPreference) {
        this.mContext = context;
        this.mSwitch = masterSwitchPreference;
        this.mIntentFilter = new IntentFilter("org.omnirom.device.SYSTEMUI_AIR_TRIGGER_ON");
        this.mIntentFilter.addAction("org.omnirom.device.SYSTEMUI_AIR_TRIGGER_OFF");
    }

    public void resume() {
        handleStateChanged(getSensorState());
        this.mSwitch.setOnPreferenceChangeListener(this);
        this.mContext.registerReceiver(this.mReceiver, this.mIntentFilter);
    }

    public void pause() {
        this.mSwitch.setOnPreferenceChangeListener(null);
        this.mContext.unregisterReceiver(this.mReceiver);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object obj) {
        boolean booleanValue = ((Boolean) obj).booleanValue();
        this.mSwitch.setEnabled(false);
        setSensorEnabled(booleanValue);
        return true;
    }

    private void setSensorEnabled(boolean z) {
        handleStateChanged(z);
        new Thread(new setPropTask(z)).start();
    }

    private boolean getSensorState() {
        return AirTriggerUtils.isMainSwitchEnable(this.mContext);
    }

    private void handleStateChanged(boolean z) {
        this.mSwitch.setChecked(z);
        this.mSwitch.setEnabled(true);
        if (!z) {
            notifySwitchOff("org.omnirom.device.NOTIFY_AIRTRIGGER_SWITCH_OFF");
        }
    }

    private class setPropTask implements Runnable {
        private boolean mIsEnabled;

        public setPropTask(boolean z) {
            this.mIsEnabled = z;
        }

        public void run() {
            AirTriggerUtils.getInstance(GripSensorEnabler.this.mContext).setMainSwitchEnable(this.mIsEnabled);
        }
    }

    private void notifySwitchOff(String str) {
        Log.d("GripSensorEnabler", "notifySwitchOff");
        Intent intent = new Intent();
        intent.setAction(str);
        intent.setPackage("org.omnirom.device");
        this.mContext.sendBroadcast(intent);
    }
}
