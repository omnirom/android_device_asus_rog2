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

import android.content.Context;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import androidx.preference.PreferenceFragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.TwoStatePreference;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.util.Log;

public class DeviceSettings extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    public static final String KEY_SETTINGS_PREFIX = "device_setting_";
    public static final String KEY_GLOVE_SWITCH = "glove";

    private static final String KEY_CATEGORY_SCREEN = "screen";
    private static final String KEY_FRAME_MODE = "frame_mode_key";
    public static final String KEY_GAME_GENIE = "game_toolbar_app";
    public static final String VENDOR_FPS = "vendor.asus.dfps";
    public static final String TEMP_FPS = "temp_fps";

    public static final String DEFAULT_FPS_VALUE = "60";
    public static final String FPS_VALUE_90 = "90";
    public static final String FPS_VALUE_120 = "120";
    private static String OMNIFPS = "OmniFrameRateIndex";

    private static ListPreference mFrameModeRate;
    private static TwoStatePreference mGloveModeSwitch;
    private static Preference mGameGenie;
    private MakeFps mMakeFps;

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

        mGameGenie = findPreference(KEY_GAME_GENIE);
        mGameGenie.setEnabled(GameGenie.isGameGenieExist(this.getContext()));

    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
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
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        final String defaultValue = DEFAULT_FPS_VALUE;

        if (value == null) {
            value = defaultValue;
        }
        try {
            String[] parts = value.split(",");
            parts[position] = String.valueOf(fps);
            String newValue = TextUtils.join(",", parts);
            Settings.System.putString(getContext().getContentResolver(), TEMP_FPS, newValue);
            mMakeFps.changeFps(sharedPrefs, Integer.valueOf(newValue));
        } catch (Exception e) {
        }
    }
}
