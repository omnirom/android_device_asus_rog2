/*
* Copyright (C) 2013 The OmniROM Project
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemProperties;
import androidx.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;

public class Startup extends BroadcastReceiver {
    private static void restore(String file, boolean enabled) {
        if (file == null) {
            return;
        }
        Utils.writeValue(file, enabled ? "1" : "0");
    }

    private static void restore(String file, String value) {
        if (file == null) {
            return;
        }
        Utils.writeValue(file, value);
    }

    private static String getGestureFile(String key) {
        return GestureSettings.getGestureFile(key);
    }

    private void maybeImportOldSettings(Context context) {
        boolean imported = Settings.System.getInt(context.getContentResolver(), "omni_device_setting_imported", 0) != 0;
        if (!imported) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean enabled = sharedPrefs.getBoolean(DeviceSettings.KEY_GLOVE_SWITCH, false);
            Settings.System.putInt(context.getContentResolver(), GloveModeSwitch.SETTINGS_KEY, enabled ? 1 : 0);

            Settings.System.putInt(context.getContentResolver(), "omni_device_setting_imported", 1);
        }
    }

    @Override
    public void onReceive(final Context context, final Intent bootintent) {
        maybeImportOldSettings(context);
        restoreAfterUserSwitch(context);
    }

    public static void restoreAfterUserSwitch(Context context) {

        // C Gesture
        String mapping = GestureSettings.DEVICE_GESTURE_MAPPING_0;
        String value = Settings.System.getString(context.getContentResolver(), mapping);
        if (TextUtils.isEmpty(value)) {
            value = AppSelectListPreference.DISABLED_ENTRY;
            Settings.System.putString(context.getContentResolver(), mapping, value);
        }
        boolean enabled = !value.equals(AppSelectListPreference.DISABLED_ENTRY);
        restore(GestureSettings.getGestureFile(GestureSettings.KEY_C_APP), enabled);

        // E Gesture
        mapping = GestureSettings.DEVICE_GESTURE_MAPPING_1;
        value = Settings.System.getString(context.getContentResolver(), mapping);
        if (TextUtils.isEmpty(value)) {
            value = AppSelectListPreference.DISABLED_ENTRY;
            Settings.System.putString(context.getContentResolver(), mapping, value);
        }
        enabled = !value.equals(AppSelectListPreference.DISABLED_ENTRY);
        restore(GestureSettings.getGestureFile(GestureSettings.KEY_E_APP), enabled);

        // S Gesture
        mapping = GestureSettings.DEVICE_GESTURE_MAPPING_2;
        value = Settings.System.getString(context.getContentResolver(), mapping);
        if (TextUtils.isEmpty(value)) {
            value = AppSelectListPreference.DISABLED_ENTRY;
            Settings.System.putString(context.getContentResolver(), mapping, value);
        }
        enabled = !TextUtils.isEmpty(value) && !value.equals(AppSelectListPreference.DISABLED_ENTRY);
        restore(GestureSettings.getGestureFile(GestureSettings.KEY_S_APP), enabled);

        // V Gesture
        mapping = GestureSettings.DEVICE_GESTURE_MAPPING_3;
        value = Settings.System.getString(context.getContentResolver(), mapping);
        if (TextUtils.isEmpty(value)) {
            value = AppSelectListPreference.DISABLED_ENTRY;
            Settings.System.putString(context.getContentResolver(), mapping, value);
        }
        enabled = !value.equals(AppSelectListPreference.DISABLED_ENTRY);
        restore(GestureSettings.getGestureFile(GestureSettings.KEY_V_APP), enabled);

        // W Gesture
        mapping = GestureSettings.DEVICE_GESTURE_MAPPING_4;
        value = Settings.System.getString(context.getContentResolver(), mapping);
        if (TextUtils.isEmpty(value)) {
            value = AppSelectListPreference.DISABLED_ENTRY;
            Settings.System.putString(context.getContentResolver(), mapping, value);
        }
        enabled = !value.equals(AppSelectListPreference.DISABLED_ENTRY);
        restore(GestureSettings.getGestureFile(GestureSettings.KEY_W_APP), enabled);

        // Z Gesture
        mapping = GestureSettings.DEVICE_GESTURE_MAPPING_5;
        value = Settings.System.getString(context.getContentResolver(), mapping);
        if (TextUtils.isEmpty(value)) {
            value = AppSelectListPreference.DISABLED_ENTRY;
            Settings.System.putString(context.getContentResolver(), mapping, value);
        }
        enabled = !value.equals(AppSelectListPreference.DISABLED_ENTRY);
        restore(GestureSettings.getGestureFile(GestureSettings.KEY_Z_APP), enabled);

        value = Settings.System.getString(context.getContentResolver(), DeviceSettings.TEMP_FPS);
        if (TextUtils.isEmpty(value)) {
            value = DeviceSettings.DEFAULT_FPS_VALUE;
            Settings.System.putString(context.getContentResolver(), DeviceSettings.TEMP_FPS, value);
            SystemProperties.set(DeviceSettings.VENDOR_FPS, value);
        } else {
        Settings.System.putString(context.getContentResolver(), DeviceSettings.TEMP_FPS, value);
        SystemProperties.set(DeviceSettings.VENDOR_FPS, value);
        }

        enabled = Settings.System.getInt(context.getContentResolver(), GloveModeSwitch.SETTINGS_KEY, 0) != 0;
        if (enabled) {
            restore(GloveModeSwitch.getFile(), enabled);
        }

        enabled = Settings.System.getInt(context.getContentResolver(), GestureSettings.SETTINGS_GESTURE_KEY, 0) != 0;
        restore(GestureSettings.getFile(), enabled);
    }
}
