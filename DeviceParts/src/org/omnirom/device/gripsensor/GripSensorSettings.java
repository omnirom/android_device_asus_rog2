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
import com.android.settingslib.core.AbstractPreferenceController;
import org.omnirom.device.Utils;
import org.omnirom.device.gripsensor.AirTriggerUtils;
import org.omnirom.device.gripsensor.GripUtils;
import org.omnirom.device.gripsensor.SqueezeFragment;
import org.omnirom.device.gripsensorservice.GripSensorService;
import org.omnirom.device.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class GripSensorSettings extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    public static final String KEY_GAME_SPACE = "pref_game_space";
    public static final String KEY_GAME_CENTER = "air_trigger_game_center";
    public static final String KEY_LIGHT_PRESS = "gripsensor_lightpress_trigger_unlock";
    public static final String KEY_HEAVY_PRESS = "gripsensor_heavypress_trigger_unlock";
    public static final String KEY_LIGHT_APP = "gripsensor_lightpress_app";
    public static final String KEY_HEAVY_APP = "gripsensor_heavypress_app";
    public static final String KEY_SUMMARY_GRIP = "gripsensor_grip_strength_adjust";
    public static final String KEY_SUMMARY_TAP = "gripsensor_single_point_strength_adjust";

    public static final String SQUEEZE_PATH = "/proc/driver/grip_squeeze_en";
    public static final String SQUEEZE1_PATH = "/proc/driver/grip_squeeze1_en";
    public static final String SQUEEZE2_PATH = "/proc/driver/grip_squeeze2_en";

    public static final String GRIP_SHORT_SQUEEZE = "short_squeeze";
    public static final String GRIP_LONG_SQUEEZE = "long_squeeze";

    private AppSelectListPreference mLightPress;
    private AppSelectListPreference mHeavyPress;
    private Bundle data = new Bundle();
    private LayoutPreference mTutorial;
    private Preference mSummartGrip;
    private Preference mSummartTap;
    private SwitchPreference mGameSpacePref;
    private Switch mSwitch;

    private List<AppSelectListPreference.PackageItem> mInstalledPackages = new LinkedList<AppSelectListPreference.PackageItem>();
    private PackageManager mPm;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.grip_settings, rootKey);
        mPm = getContext().getPackageManager();

        mGameSpacePref = (SwitchPreference) findPreference(KEY_GAME_SPACE);
        mGameSpacePref.setChecked(Settings.Global.getInt(getContext().getContentResolver(),
        KEY_GAME_CENTER, 0) == 1);
        mGameSpacePref.setOnPreferenceChangeListener(this);

        mLightPress = (AppSelectListPreference) findPreference(KEY_LIGHT_PRESS);
        mLightPress.setEnabled(true);
        String value = Settings.System.getString(getContext().getContentResolver(), KEY_LIGHT_PRESS);
        mLightPress.setValue(value);
        mLightPress.setOnPreferenceChangeListener(this);

        mHeavyPress = (AppSelectListPreference) findPreference(KEY_HEAVY_PRESS);
        mHeavyPress.setEnabled(true);
        value = Settings.System.getString(getContext().getContentResolver(), KEY_HEAVY_PRESS);
        mHeavyPress.setValue(value);
        mHeavyPress.setOnPreferenceChangeListener(this);

        mSummartGrip = (Preference) findPreference (KEY_SUMMARY_GRIP);
        int squeezeThresholdLevel = AirTriggerUtils.getInstance(this.getContext()).getSqueezeThresholdLevel();
        mSummartGrip.setSummary(String.format(this.getContext().getResources().getString(R.string.force_level), String.valueOf(squeezeThresholdLevel)));

        mSummartTap = (Preference) findPreference (KEY_SUMMARY_TAP);
        int tapThresholdLevel = AirTriggerUtils.getInstance(this.getContext()).getTapThresholdLevel(1);
        int tapThresholdLevel2 = AirTriggerUtils.getInstance(this.getContext()).getTapThresholdLevel(2);
        mSummartTap.setSummary(String.format(this.getContext().getResources().getString(R.string.summary_tap_left),
                               String.valueOf(tapThresholdLevel)) + " " + String.format(getResources().getString(R.string.summary_tap_right),
                               String.valueOf(tapThresholdLevel2)));

        initSwitchState();
        collectAirTriggerState();
        GripSensorService.enqueueWork(getContext(), new Intent(getContext(), GripSensorService.class));
        new FetchPackageInformationTask().execute();

    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
         if (preference == mGameSpacePref) {
            Utils.writeValue(SQUEEZE_PATH, mGameSpacePref.isChecked() ? "1" : "0");
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mLightPress) {
            String value = (String) newValue;
            Settings.System.putString(getContext().getContentResolver(), KEY_LIGHT_PRESS, value);
            if (value == AppSelectListPreference.DISABLED_ENTRY) {
                SqueezeFragment.setSwitchState(getContext(), 3, false);
                Utils.writeValue(SQUEEZE1_PATH, "0");
            } else {
                SqueezeFragment.setSwitchState(getContext(), 3, true);
                Utils.writeValue(SQUEEZE1_PATH, "1");
            }
            AirTriggerUtils.setSettingsProviderForGrip(getContext().getContentResolver(), value, String.valueOf(0));
        } else if (preference == mHeavyPress) {
            String value = (String) newValue;
            Settings.System.putString(getContext().getContentResolver(), KEY_HEAVY_PRESS, value);
            if (value == AppSelectListPreference.DISABLED_ENTRY) {
                SqueezeFragment.setSwitchState(getContext(), 4, false);
                Utils.writeValue(SQUEEZE2_PATH, "0");
            } else {
                SqueezeFragment.setSwitchState(getContext(), 4, true);
                Utils.writeValue(SQUEEZE2_PATH, "1");
            }
            AirTriggerUtils.setSettingsProviderForGrip(getContext().getContentResolver(), value, String.valueOf(0));
        }
        if (preference == mGameSpacePref) {
            Boolean enabled = (Boolean) newValue;
            Settings.Global.putInt(getContext().getContentResolver(), KEY_GAME_CENTER, enabled ? 1 : 0);
            boolean activate = Settings.Global.getInt(getContext().getContentResolver(),
                                                      KEY_GAME_CENTER, 1) == 1;
            if (activate) {
                AirTriggerUtils.getInstance(getContext()).setGameSpaceEnable(true);
            } else {
                AirTriggerUtils.getInstance(getContext()).setGameSpaceEnable(false);
                
            }
        }
        return true;
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (!(preference instanceof AppSelectListPreference)) {
            super.onDisplayPreferenceDialog(preference);
            return;
        }
        DialogFragment fragment =
                AppSelectListPreference.AppSelectListPreferenceDialogFragment
                        .newInstance(preference.getKey());
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), "dialog_preference");
    }

    private void loadInstalledPackages() {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> installedAppsInfo = mPm.queryIntentActivities(mainIntent, 0);

        for (ResolveInfo info : installedAppsInfo) {
            ActivityInfo activity = info.activityInfo;
            ApplicationInfo appInfo = activity.applicationInfo;
            ComponentName componentName = new ComponentName(appInfo.packageName, activity.name);
            CharSequence label = null;
            try {
                label = activity.loadLabel(mPm);
            } catch (Exception e) {
            }
            if (label != null) {
                final AppSelectListPreference.PackageItem item = new AppSelectListPreference.PackageItem(activity.loadLabel(mPm), 0, componentName);
                mInstalledPackages.add(item);
            }
        }
        Collections.sort(mInstalledPackages);
    }

    private class FetchPackageInformationTask extends AsyncTask<Void, Void, Void> {
        public FetchPackageInformationTask() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            loadInstalledPackages();
            return null;
        }

        @Override
        protected void onPostExecute(Void feed) {
            mLightPress.setPackageList(mInstalledPackages);
            mHeavyPress.setPackageList(mInstalledPackages);
        }
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
        ContentResolver contentResolver = getContext().getContentResolver();
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

    private void collectAirTriggerState() {
        ContentResolver contentResolver = getContext().getContentResolver();
        boolean z = false;
        data.putBoolean("air_trigger_enable", Settings.Global.getInt(contentResolver, "air_trigger_enable", 0) == 1);
        data.putInt("air_trigger_squeeze_threshold_level", Settings.Global.getInt(contentResolver, "air_trigger_squeeze_threshold_level", 0));
        data.putInt("air_trigger_tap_left_threshold_level", Settings.Global.getInt(contentResolver, "air_trigger_tap_left_threshold_level", 0));
        data.putInt("air_trigger_tap_right_threshold_level", Settings.Global.getInt(contentResolver, "air_trigger_tap_right_threshold_level", 0));
        data.putBoolean("air_trigger_sensitivity_booster_enable", Settings.Global.getInt(contentResolver, "air_trigger_sensitivity_booster_enable", 0) == 1);
        data.putBoolean("air_trigger_game_center", Settings.Global.getInt(contentResolver, "air_trigger_game_center", 0) == 1);
        data.putBoolean("air_trigger_allow_screen_off_short", Settings.Global.getInt(contentResolver, "air_trigger_allow_screen_off_short", 1) == 1);
        if (Settings.Global.getInt(contentResolver, "air_trigger_allow_screen_off_long", 1) == 1) {
            z = true;
        }
        data.putBoolean("air_trigger_allow_screen_off_long", z);
        String value = Settings.Global.getString(getContext().getContentResolver(), "asus_gamemode");
        final String defaultValue = "0";

        Settings.Global.putString(getContext().getContentResolver(), "asus_gamemode", defaultValue);
        data.putBoolean("air_trigger_short_squeeze_enable", SqueezeFragment.isSwitchEnabled(contentResolver, 3));
        data.putBoolean("air_trigger_long_squeeze_enable", SqueezeFragment.isSwitchEnabled(contentResolver, 4));
        data.putInt("air_trigger_short_squeeze_function", SqueezeFragment.getIdMap(getContext(), 3));
        data.putInt("air_trigger_long_squeeze_function", SqueezeFragment.getIdMap(getContext(), 4));
    }
}
