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

public class GripTapForce extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {

     public static final String GRIP_TAPFORCE_LEFT_LEFT = "griptap_force_left";
     public static final String GRIP_TAPFORCE_LEFT_RIGHT = "griptap_force_right";

     private GripTapForcePreference mGripTapForce;
     private GripTapForceRightPreference mGripTapForceR;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.grip_tap_force, rootKey);

        mGripTapForce = (GripTapForcePreference) findPreference(GRIP_TAPFORCE_LEFT_LEFT);
        if (mGripTapForce != null) {
            mGripTapForce.setEnabled(GripTapForcePreference.isSupported());
        }

        mGripTapForceR = (GripTapForceRightPreference) findPreference(GRIP_TAPFORCE_LEFT_RIGHT);
        if (mGripTapForceR != null) {
            mGripTapForceR.setEnabled(GripTapForceRightPreference.isSupported());
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }
}
