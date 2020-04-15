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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.preference.PreferenceFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class GripStrengthAdjustActivity extends Activity {

    private GripStrengthAdjust mGripStrengthAdjustFragment;
    protected int mRequestType = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("GripStrengthAdjustAct", "onCreate");
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        Fragment fragment = getFragmentManager().findFragmentById(android.R.id.content);
        if (fragment == null) {
            mGripStrengthAdjustFragment = new GripStrengthAdjust();
            getFragmentManager().beginTransaction()
                .add(android.R.id.content, mGripStrengthAdjustFragment)
                .commit();
        } else {
            mGripStrengthAdjustFragment = (GripStrengthAdjust) fragment;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Intent getIntent() {
        Intent intent = new Intent(super.getIntent());
        intent.putExtra(":device:show_fragment", GripStrengthAdjust.class.getName());
        return intent;
    }

    @Override
    protected void onResume() {
        Log.d("GripStrengthAdjustAct", "onResume");
        super.onResume();
        if (getIntent().getIntExtra("request_code", 1) == 2) {
            notifyGripServiceSettingPage("org.omnirom.device.NOTIFY_GRIP_SETTING_PAGE");
        }
        AirTriggerUtils.injectKeycode(853);
        AirTriggerUtils.getInstance(getApplication()).setGripStengthPage(true);
    }

    @Override
    protected void onPause() {
        Log.d("GripStrengthAdjustAct", "onPause");
        super.onPause();
        if (getIntent().getIntExtra("request_code", 1) == 2) {
            notifyGripServiceSettingPage("org.omnirom.device.NOTIFY_LEAVE_GRIP_SETTING_PAGE");
        }
        AirTriggerUtils.injectKeycode(851);
        AirTriggerUtils.getInstance(getApplication()).setGripStengthPage(false);
    }

    @Override // androidx.fragment.app.FragmentActivity, org.omnirom.device.core.SettingsBaseActivity
    protected void onDestroy() {
        Log.d("GripStrengthAdjustAct", "onDestroy");
        super.onDestroy();
    }

    private void notifyGripServiceSettingPage(String str) {
        Log.d("GripStrengthAdjustAct", "notify GripSensorService to show/hide grip floating view");
        Intent intent = new Intent();
        intent.setAction(str);
        intent.setPackage("org.omnirom.device");
        sendBroadcast(intent);
    }
}
