/*
* Copyright (C) 2023 The OmniROM Project
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

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import androidx.preference.PreferenceFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.TwoStatePreference;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

public class AudioSettings extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "AudioWizardControllerOmni";
    private static final String KEY_SETTINGS_PREFIX = "device_setting_";
    private static final String KEY_AUDIOWIZARD = "audiowizard_entry";
    private static final String KEY_OUTDOOR_MODE = "outdoor_mode";
    private static final String PACKAGE_NAME = "com.asus.maxxaudio.audiowizard";
    private static final String SERVICE_NAME = "com.asus.maxxaudio";
    private static final String SETTINGS_KEY_AUDIO_WIZARD_DEVICE = "settings_key_audio_wizard_device";
    private static final String SETTINGS_KEY_AUDIO_WIZARD_HEADSET_EFFECT = "settings_key_audio_wizard_headset_effect";
    private static final String SETTINGS_KEY_AUDIO_WIZARD_MODE = "settings_key_audio_wizard_mode";
    private static final String SETTINGS_KEY_AUDIO_WIZARD_OUTDOOR_MODE = "audio_wizard_outdoor_mode";

    private static final int CUSTOM_MODE_ONE_ID = 101;
    private static final int MODE_CLASSICAL = 4;
    private static final int MODE_DANCE = 5;
    private static final int MODE_JAZZ = 6;
    private static final int MODE_NORMAL = 1;
    private static final int MODE_POP = 7;
    private static final int MODE_ROCK = 8;
    private static final int MODE_VOCAL = 9;
    private static final int SETTINGS_AUDIO_WIZARD_OUTDOOR_MODE_OFF = 1;
    private static final int SETTINGS_AUDIO_WIZARD_OUTDOOR_MODE_OFF_PANEL = 0;
    private static final int SETTINGS_AUDIO_WIZARD_OUTDOOR_MODE_ON = 2;
    private static final int STEREO_MODE_FRONT = 1;
    private static final int STEREO_MODE_PURE = -1;
    private static final int STEREO_MODE_TRADITIONAL = 2;
    private static final int STEREO_MODE_TURNON = 3;
    private static final int STEREO_MODE_WIDE = 0;

    private final int DEFAULT_VALUE = 0;
    private final int OUTDOOR_MODE_ON = 2;
    private int mActiveRouteNumber = -1;

    private TwoStatePreference mOutdoorMode;
    private Preference mWizard;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.audio_mode, rootKey);
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mOutdoorMode = (TwoStatePreference) findPreference(KEY_OUTDOOR_MODE);
        mOutdoorMode.setChecked(Settings.System.getInt(getContext().getContentResolver(),
            SETTINGS_KEY_AUDIO_WIZARD_OUTDOOR_MODE,
            SETTINGS_AUDIO_WIZARD_OUTDOOR_MODE_OFF_PANEL)==
            SETTINGS_AUDIO_WIZARD_OUTDOOR_MODE_ON);
        mOutdoorMode.setOnPreferenceChangeListener(this);

        mWizard = prefScreen.findPreference(KEY_AUDIOWIZARD);
        mWizard.setEnabled(isEnable());
        updateState();

    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mOutdoorMode) {
            Settings.System.putInt(getContext().getContentResolver(),
                SETTINGS_KEY_AUDIO_WIZARD_OUTDOOR_MODE, mOutdoorMode.isChecked() ?
                SETTINGS_AUDIO_WIZARD_OUTDOOR_MODE_ON :
                SETTINGS_AUDIO_WIZARD_OUTDOOR_MODE_OFF_PANEL);
            updateState();
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }

    private void updateState() {
        if (isEnable()) {
            mWizard.setEnabled(true);
        } else {
            mWizard.setEnabled(false);
        }
        String summaryString = getSummaryString();
        Log.i(TAG, "updateState summary = " + summaryString);
        if (!TextUtils.isEmpty(summaryString)) {
            mWizard.setSummary(summaryString);
        } else {
            mWizard.setSummary("");
        }
    }

    private boolean isEnable() {
        try {
            return getContext().getPackageManager().getApplicationInfo(SERVICE_NAME, 0).enabled
                && getContext().getPackageManager().getApplicationInfo(PACKAGE_NAME, 0).enabled;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String getSummaryString() {
        String modeName;
        mActiveRouteNumber = Settings.System.getInt(getContext().getContentResolver(), SETTINGS_KEY_AUDIO_WIZARD_DEVICE, -1);
        Log.i(TAG, "getSummaryString() mActiveRouteNumber = " + mActiveRouteNumber);
        if (mActiveRouteNumber == -1) {
            Log.i(TAG, "getSummaryString() active route is unknown, return");
            return "";
        }
        int i = Settings.System.getInt(getContext().getContentResolver(), SETTINGS_KEY_AUDIO_WIZARD_MODE, -1);
        Log.i(TAG, "getSummaryString() mode = " + i);
        if (mActiveRouteNumber == OUTDOOR_MODE_ON) {
            if (Settings.System.getInt(getContext().getContentResolver(),
                    SETTINGS_KEY_AUDIO_WIZARD_OUTDOOR_MODE,
                    SETTINGS_AUDIO_WIZARD_OUTDOOR_MODE_OFF) ==
                    SETTINGS_AUDIO_WIZARD_OUTDOOR_MODE_ON) {
                modeName = getResources().getString(R.string.outdoor_mode_title);
            } else {
                modeName = getModeName(i);
            }
            return modeName;
        }
        int i2 = Settings.System.getInt(getContext().getContentResolver(), SETTINGS_KEY_AUDIO_WIZARD_HEADSET_EFFECT, -1);
        Log.d(TAG, "getSummaryString() headsetEffect = " + i2);
        return i2 != -1 ? String.format(getResources().getString(
                        R.string.audiowizard_summary_headset_effect_and_mode),
                        getHeadsetEffectName(i2), getModeName(i)) : "";
    }

    private String getHeadsetEffectName(int i) {
        if (i != STEREO_MODE_WIDE) {
            if (i != STEREO_MODE_FRONT) {
                return i != STEREO_MODE_TRADITIONAL ? "" : getResources().getString(R.string.audio_traditional);
            }
            return getResources().getString(R.string.audio_in_front);
        }
        return getResources().getString(R.string.audio_wide);
    }

    private String getModeName(int i) {
        if (i != MODE_NORMAL) {
            if (i != CUSTOM_MODE_ONE_ID) {
                switch (i) {
                    case MODE_CLASSICAL:
                        return getResources().getString(R.string.mode_classical);
                    case MODE_DANCE:
                        return getResources().getString(R.string.mode_dance);
                    case MODE_JAZZ:
                        return getResources().getString(R.string.mode_jazz);
                    case MODE_POP:
                        return getResources().getString(R.string.mode_pop);
                    case MODE_ROCK:
                        return getResources().getString(R.string.mode_rock);
                    case MODE_VOCAL:
                        return getResources().getString(R.string.mode_vocal);
                    default:
                        return "";
                }
            }
            return getResources().getString(R.string.custom_name);
        }
        return getResources().getString(R.string.mode_normal);
    }
}
