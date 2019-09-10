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
import android.content.res.Resources;
import android.content.Intent;
import android.os.Bundle;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.TwoStatePreference;
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

    public static final String SLIDER_DEFAULT_VALUE = "2,1,0";

    public static final String KEY_SETTINGS_PREFIX = "device_setting_";
    public static final String KEY_GLOVE_SWITCH = "glove";
    public static final String KEY_SMART_SWITCH = "smart_switch";
    public static final String KEY_SMART_PATH = "/sys/devices/platform/soc/soc:asustek_googlekey/googlekey_enable";
    public static final String SETTINGS_SMART_KEY = KEY_SETTINGS_PREFIX + KEY_SMART_SWITCH;

    private static final String KEY_CATEGORY_SCREEN = "screen";
    private static TwoStatePreference mGloveModeSwitch;
    private static TwoStatePreference mSmartKeySwitch;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.main, rootKey);

        mGloveModeSwitch = (TwoStatePreference) findPreference(KEY_GLOVE_SWITCH);
        mGloveModeSwitch.setEnabled(GloveModeSwitch.isSupported());
        mGloveModeSwitch.setChecked(GloveModeSwitch.isCurrentlyEnabled(this.getContext()));
        mGloveModeSwitch.setOnPreferenceChangeListener(new GloveModeSwitch(getContext()));

        mSmartKeySwitch = (TwoStatePreference) findPreference(KEY_SMART_SWITCH);
        mSmartKeySwitch.setChecked(Settings.System.getInt(getContext().getContentResolver(),
        SETTINGS_SMART_KEY, 1) != 0);

    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mSmartKeySwitch) {
            Settings.System.putInt(getContext().getContentResolver(), SETTINGS_SMART_KEY, mSmartKeySwitch.isChecked() ? 1 : 0);
            Utils.writeValue(getFile(), mSmartKeySwitch.isChecked() ? "1" : "0");
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }

    public static String getFile() {
        if (Utils.fileWritable(KEY_SMART_PATH)) {
            return KEY_SMART_PATH;
        }
        return null;
    }

    public static String getGestureFile(String key) {
        switch(key) {
            case KEY_SMART_PATH:
                return "/sys/devices/platform/soc/soc:asustek_googlekey/googlekey_enable";
        }
        return null;
    }
}
