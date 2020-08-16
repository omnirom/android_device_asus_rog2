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

import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import android.util.Log;

public class Airtrigger {

    private static final String AIRTRIGGER_PACKAGE_NAME = "com.asus.airtriggers";
    private static final String ACTION_START_AIRTRIGGER_SETTINGS = "asus.intent.action.AIRTRIGGER";
    private static final String TAG = "AirTriggerApkPreferenceController";

    private Context mContext;

    public Airtrigger(Context context) {
        mContext = context;
    }

    protected static void startAirTriggerSettings(Context context) {
        try {
            context.startActivity(new Intent(ACTION_START_AIRTRIGGER_SETTINGS).setPackage(AIRTRIGGER_PACKAGE_NAME).addFlags(335544320), ActivityOptions.makeBasic().setLaunchDisplayId(0).toBundle());
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "AirTrigger apk activity not found exception : " + e.toString());
        }
    }
}