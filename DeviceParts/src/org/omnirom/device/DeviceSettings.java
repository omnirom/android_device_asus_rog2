/*
* Copyright (C) 2016 The OmniROM Project
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
*/
package org.omnirom.device;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.SystemProperties;
import androidx.preference.PreferenceFragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.TwoStatePreference;
import androidx.preference.SwitchPreference;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.util.Log;
import org.omnirom.device.gripsensor.AirTriggerUtils;

public class DeviceSettings extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    public static final String KEY_SETTINGS_PREFIX = "device_setting_";
    public static final String KEY_GLOVE_SWITCH = "glove";

    private static final String KEY_CATEGORY_SCREEN = "screen";
    private static final String KEY_FRAME_MODE = "frame_mode_key";
    public static final String KEY_GAME_GENIE = "game_toolbar_app";
    public static final String KEY_MAIN_GRIP = "grip_sensor";
    public static final String KEY_AIR_TRIGGER = "air_trigger_enable";
    public static final String VENDOR_FPS = "vendor.asus.dfps";
    public static final String TEMP_FPS = "temp_fps";

    public static final String GRIP_PATH = "/proc/driver/grip_en";
    public static final String GRIP_PATH_2 = "/proc/driver/grip_raw_en";
    public static final String GRIP_PATH_3 = "/proc/driver/grip_tap_sense_en";
    public static final String GRIP_TAP_PATH = "/proc/driver/grip_tap1_en";
    public static final String GRIP_TAP2_PATH = "/proc/driver/grip_tap2_en";
    public static final String GRIP_VIB_PATH = "/proc/driver/grip_tap1_vib_en";
    public static final String GRIP_VIB2_PATH = "/proc/driver/grip_tap2_vib_en";

    public static final String DEFAULT_FPS_VALUE = "60";
    public static final String FPS_VALUE_90 = "90";
    public static final String FPS_VALUE_120 = "120";

    private static ListPreference mFrameModeRate;
    private static SwitchPreference mGripSensorPreference;
    private static TwoStatePreference mGloveModeSwitch;
    private static Preference mGameGenie;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.main, rootKey);

        mGloveModeSwitch = (TwoStatePreference) findPreference(KEY_GLOVE_SWITCH);
        mGloveModeSwitch.setEnabled(GloveModeSwitch.isSupported());
        mGloveModeSwitch.setChecked(GloveModeSwitch.isCurrentlyEnabled(this.getContext()));
        mGloveModeSwitch.setOnPreferenceChangeListener(new GloveModeSwitch(getContext()));

        mFrameModeRate = (ListPreference) findPreference(KEY_FRAME_MODE);
        mFrameModeRate.setOnPreferenceChangeListener(this);
        int frameMode = getFrameMode(0);
        int valueIndex = mFrameModeRate.findIndexOfValue(String.valueOf(frameMode));
        mFrameModeRate.setValueIndex(valueIndex);
        mFrameModeRate.setSummary(mFrameModeRate.getEntries()[valueIndex]);

        mGripSensorPreference = (SwitchPreference) findPreference(KEY_MAIN_GRIP);
        mGripSensorPreference.setChecked(Settings.Global.getInt(getContext().getContentResolver(),
        KEY_AIR_TRIGGER, 0) == 1);
        mGripSensorPreference.setOnPreferenceChangeListener(this);

        mGameGenie = findPreference(KEY_GAME_GENIE);
        mGameGenie.setEnabled(GameGenie.isGameGenieExist(this.getContext()));

    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mGripSensorPreference) {
            SystemProperties.set("persist.asus.hardware.gripsensor", "1");
            Utils.writeValue(GRIP_PATH, mGripSensorPreference.isChecked() ? "1" : "0");
            Utils.writeValue(GRIP_PATH_2, mGripSensorPreference.isChecked() ? "1" : "0");
            Utils.writeValue(GRIP_PATH_3, mGripSensorPreference.isChecked() ? "1" : "0");
            Utils.writeValue(GRIP_TAP_PATH, mGripSensorPreference.isChecked() ? "1" : "0");
            Utils.writeValue(GRIP_TAP2_PATH, mGripSensorPreference.isChecked() ? "1" : "0");
            Utils.writeValue(GRIP_VIB_PATH, mGripSensorPreference.isChecked() ? "1" : "0");
            Utils.writeValue(GRIP_VIB2_PATH, mGripSensorPreference.isChecked() ? "1" : "0");
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mFrameModeRate) {
            String value = (String) newValue;
            int frameMode = Integer.valueOf(value);
            setFrameMode(0, frameMode);
            int valueIndex = mFrameModeRate.findIndexOfValue(value);
            mFrameModeRate.setSummary(mFrameModeRate.getEntries()[valueIndex]);
        }
        if (preference == mGripSensorPreference) {
            Boolean enabled = (Boolean) newValue;
            Settings.Global.putInt(getContext().getContentResolver(), KEY_AIR_TRIGGER, enabled ? 1 : 0);
            boolean activate = Settings.Global.getInt(getContext().getContentResolver(),
                                                      KEY_AIR_TRIGGER, 1) == 1;
            if (activate) {
                AirTriggerUtils.getInstance(getContext()).setMainSwitchEnable(true);
            } else {
                AirTriggerUtils.getInstance(getContext()).setMainSwitchEnable(false);
                notifySwitchOff("org.omnirom.device.NOTIFY_AIRTRIGGER_SWITCH_OFF");
            }
        }
        return true;
    }

    private int getFrameMode(int position) {

        String value = Settings.System.getString(getContext().getContentResolver(), TEMP_FPS);
        final String defaultValue = DEFAULT_FPS_VALUE;

        if (value == null) {
            value = defaultValue;
        }
        try {
            String[] parts = value.split(",");
            return Integer.valueOf(parts[position]);
        } catch (Exception e) {
        }
        return 0;
    }

    private void setFrameMode(int position, int fps) {

        String value = Settings.System.getString(getContext().getContentResolver(), TEMP_FPS);
        final String defaultValue = DEFAULT_FPS_VALUE;

        if (value == null) {
            value = defaultValue;
        }
        try {
            String[] parts = value.split(",");
            parts[position] = String.valueOf(fps);
            String newValue = TextUtils.join(",", parts);
            Settings.System.putString(getContext().getContentResolver(), TEMP_FPS, newValue);
            SystemProperties.set(VENDOR_FPS, newValue);
        } catch (Exception e) {
        }
    }

    private void notifySwitchOff(String str) {
        Log.d("GripSensorEnabler", "notifySwitchOff");
        Intent intent = new Intent();
        intent.setAction(str);
        intent.setPackage("org.omnirom.device");
        getContext().sendBroadcastAsUser(intent, UserHandle.CURRENT);
    }
}
