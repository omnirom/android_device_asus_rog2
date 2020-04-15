/*
* Copyright (C) 2020 The OmniROM Project
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
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.preference.PreferenceFragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.util.Log;
import android.os.UserHandle;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class GripSensorSettings extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    public static final String KEY_GRIP_SWITCH = "grip_switch";
    public static final String KEY_GRIP_FORCE = "grip_mode_key";
    public static final String GRIP_LEVEL = "air_trigger_squeeze_threshold_level";
    public static final String DEFAULT_GRIP_VALUE = "60";

    private static ListPreference mGripSqueezeForce;
    private SwitchPreference mGripSwitch;

    public static final String GRIP_PATH = "/proc/driver/grip_en";
    public static final String GRIP_PATH_2 = "/proc/driver/grip_raw_en";
    public static final String GRIP_PATH_3 = "/proc/driver/grip_squeeze_en";
    public static final String GRIP_PATH_4 = "/proc/driver/grip_tap_sense_en";
    public static final String GRIP_SQUEEZE_FORCE_PATH = "/proc/driver/grip_squeeze_force";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.grip_settings, rootKey);
        Resources res = getResources();

        mGripSwitch = (SwitchPreference) findPreference(KEY_GRIP_SWITCH);
        mGripSwitch.setChecked(Settings.System.getInt(getContext().getContentResolver(),
        KEY_GRIP_SWITCH, 0) == 1);

        mGripSqueezeForce = (ListPreference) findPreference(KEY_GRIP_FORCE);
        mGripSqueezeForce.setOnPreferenceChangeListener(this);
        int GripForce = getGripForce(0);
        int valueIndex = mGripSqueezeForce.findIndexOfValue(String.valueOf(GripForce));
        mGripSqueezeForce.setValueIndex(valueIndex);
        mGripSqueezeForce.setSummary(mGripSqueezeForce.getEntries()[valueIndex]);

    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mGripSwitch) {
            Settings.System.putInt(getContext().getContentResolver(), KEY_GRIP_SWITCH, mGripSwitch.isChecked() ? 1 : 0);
            Utils.writeValue(GRIP_PATH, mGripSwitch.isChecked() ? "1" : "0");
            Utils.writeValue(GRIP_PATH_2, mGripSwitch.isChecked() ? "1" : "0");
            Utils.writeValue(GRIP_PATH_3, mGripSwitch.isChecked() ? "1" : "0");
            Utils.writeValue(GRIP_PATH_4, mGripSwitch.isChecked() ? "1" : "0");
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mGripSqueezeForce) {
            String value = (String) newValue;
            int GripForce = Integer.valueOf(value);
            setGripForce(0, GripForce);
            int valueIndex = mGripSqueezeForce.findIndexOfValue(value);
            mGripSqueezeForce.setSummary(mGripSqueezeForce.getEntries()[valueIndex]);
        }
        return true;
    }

    private int getGripForce(int position) {

        String value = Settings.System.getString(getContext().getContentResolver(), GRIP_LEVEL);
        final String defaultValue = DEFAULT_GRIP_VALUE;

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

    private void setGripForce(int position, int fps) {

        String value = Settings.System.getString(getContext().getContentResolver(), GRIP_LEVEL);
        final String defaultValue = DEFAULT_GRIP_VALUE;

        if (value == null) {
            value = defaultValue;
        }
        try {
            String[] parts = value.split(",");
            parts[position] = String.valueOf(fps);
            String newValue = TextUtils.join(",", parts);
            Settings.System.putString(getContext().getContentResolver(), GRIP_LEVEL, newValue);
            Utils.writeLine(GRIP_SQUEEZE_FORCE_PATH, newValue);
        } catch (Exception e) {
        }
    }
}
