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
package org.omnirom.device.gripsensor;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemProperties;
import androidx.lifecycle.Lifecycle;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Switch;
import android.util.Log;
import android.os.Bundle;
import android.os.UserHandle;
import com.android.settingslib.widget.LayoutPreference;
import org.omnirom.device.gripsensor.GripSensorEnabler;
import org.omnirom.device.widget.MasterSwitchPreference;
import org.omnirom.device.Utils;
import org.omnirom.device.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class GripSensorSettings extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    public static final String KEY_MAIN_GRIP = "grip_sensor";
    public static final String KEY_GAME_SPACE = "pref_game_space";
    public static final String KEY_LIGHT_PRESS = "gripsensor_lightpress_trigger_unlock";
    public static final String KEY_HEAVY_PRESS = "gripsensor_heavypress_trigger_unlock";
    public static final String KEY_LIGHT_APP = "gripsensor_lightpress_app";
    public static final String KEY_HEAVY_APP = "gripsensor_heavypress_app";
    public static final String KEY_SHORT_SQUEEZE = "air_trigger_short_squeeze_enable";
    public static final String KEY_LONG_SQUEEZE = "air_trigger_long_squeeze_enable";
    public static final String KEY_SUMMARY_GRIP = "gripsensor_grip_strength_adjust";

    private MasterSwitchPreference mLightPress;
    private MasterSwitchPreference mHeavyPress;
    private GripSensorEnabler mGripSensorEnabler;
    private LayoutPreference mTutorial;
    private MasterSwitchPreference mMainGripSwich;
    private Preference mSummartGrip;
    private Switch mSwitch;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.grip_settings, rootKey);

        mMainGripSwich = (MasterSwitchPreference) findPreference(KEY_MAIN_GRIP);
        mMainGripSwich.setOnPreferenceChangeListener(this);
        mGripSensorEnabler = new GripSensorEnabler(this.getContext(), mMainGripSwich);

        mSummartGrip = (Preference) findPreference (KEY_SUMMARY_GRIP);
        int squeezeThresholdLevel = AirTriggerUtils.getInstance(this.getContext()).getSqueezeThresholdLevel();
        mSummartGrip.setSummary(String.format(this.getContext().getResources().getString(R.string.force_level), String.valueOf(squeezeThresholdLevel)));

        initSwitchState();

    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mMainGripSwich) {
            SystemProperties.set("persist.asus.hardware.gripsensor", "1");
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }

    private boolean hasInit(ContentResolver contentResolver, Uri uri, int i) {
        Cursor query = contentResolver.query(ContentUris.withAppendedId(uri, (long) i), null, null, null, null);
        if (query != null && query.getCount() > 0) {
            return true;
        }
        if (query == null) {
            return false;
        }
        query.close();
        return false;
    }

    private void initSwitchState() {
        ContentResolver contentResolver = getActivity().getContentResolver();
        if (!hasInit(contentResolver, GripUtils.URI_SHORT_GRIP, 2)) {
            if (Utils.isCNSKU()) {
                GripUtils.addOrUpdateGripData(GripUtils.URI_SHORT_GRIP, contentResolver, 2, "Global_grip", -1, null, -1, null, null, -1, -1, -1, null, null, -1, null, null, 4);
                GripUtils.addOrUpdateGripData(GripUtils.URI_SHORT_GRIP, contentResolver, 1, "Global_grip_locked", -1, null, -1, null, null, -1, -1, -1, null, null, -1, null, null, 4);
            } else {
                GripUtils.addOrUpdateGripData(GripUtils.URI_SHORT_GRIP, contentResolver, 2, "Global_grip", -1, null, -1, null, null, -1, -1, 219, null, null, -1, null, null, -1);
                GripUtils.addOrUpdateGripData(GripUtils.URI_SHORT_GRIP, contentResolver, 1, "Global_grip_locked", -1, null, -1, null, null, -1, -1, 219, null, null, -1, null, null, -1);
            }
            GripUtils.addOrUpdateGripData(GripUtils.URI_LONG_GRIP, contentResolver, 2, "Global_grip", -1, null, -1, null, null, -1, -1, -1, null, null, -1, null, null, 2);
            GripUtils.addOrUpdateGripData(GripUtils.URI_LONG_GRIP, contentResolver, 1, "Global_grip_locked", -1, null, -1, null, null, -1, -1, -1, null, null, -1, null, null, 2);
            if (Utils.isCNSKU()) {
                AirTriggerUtils.setSettingsProviderForGrip(contentResolver, "asus_grip_short_squeeze", String.valueOf(6));
                AirTriggerUtils.setSettingsProviderForGrip(contentResolver, "asus_grip_locked_short_squeeze", String.valueOf(6));
            } else {
                AirTriggerUtils.setSettingsProviderForGrip(contentResolver, "asus_grip_short_squeeze", String.valueOf(4));
                AirTriggerUtils.setSettingsProviderForGrip(contentResolver, "asus_grip_locked_short_squeeze", String.valueOf(4));
            }
            AirTriggerUtils.setSettingsProviderForGrip(contentResolver, "asus_grip_long_squeeze", String.valueOf(6));
            AirTriggerUtils.setSettingsProviderForGrip(contentResolver, "asus_grip_locked_long_squeeze", String.valueOf(6));
        }
    }
}
