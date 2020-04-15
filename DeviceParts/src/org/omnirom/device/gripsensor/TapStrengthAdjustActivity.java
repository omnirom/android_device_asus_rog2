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
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import androidx.preference.PreferenceFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class TapStrengthAdjustActivity extends Activity {

    private Boolean mIsValidCreate = false;
    private TapStrengthAdjust mTapStrengthAdjustFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        Fragment fragment = getFragmentManager().findFragmentById(android.R.id.content);
        if (fragment == null) {
            mTapStrengthAdjustFragment = new TapStrengthAdjust();
            getFragmentManager().beginTransaction()
                .add(android.R.id.content, mTapStrengthAdjustFragment)
                .commit();
        } else {
            mTapStrengthAdjustFragment = (TapStrengthAdjust) fragment;
        }

        notifyGripServiceTagPage("org.omnirom.device.NOTIFY_TAP_SETTING_PAGE", "onCreate");
        Resources res = getResources();
        if (getResources().getConfiguration().orientation == 2) {
            mIsValidCreate = true;
        } else {
            mIsValidCreate = false;
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
        intent.putExtra(":device:show_fragment", TapStrengthAdjust.class.getName());
        return intent;
    }

    @Override
    protected void onResume() {
        super.onResume();
        AirTriggerUtils.injectKeycode(853);
        notifyGripServiceTagPage("org.omnirom.device.NOTIFY_TAP_SETTING_PAGE", "onResume");
        AirTriggerUtils.getInstance(getApplication()).setTapStengthPage(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (this.mIsValidCreate.booleanValue()) {
            AirTriggerUtils.injectKeycode(851);
            notifyGripServiceTagPage("org.omnirom.device.NOTIFY_LEAVE_TAP_SETTING_PAGE", "onPause");
            AirTriggerUtils.getInstance(getApplication()).setTapStengthPage(false);
        }
    }

    @Override
    public void onBackPressed() {
        notifyBackKey();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void notifyGripServiceTagPage(String str, String str2) {
        Log.d("TapStrengthAdjustAct", "notify GripSensorService to show/hide grip floating view from" + str2);
        Intent intent = new Intent();
        intent.setAction(str);
        intent.setPackage("org.omnirom.device");
        intent.putExtra("from_", str2);
        sendBroadcastAsUser(intent, UserHandle.CURRENT);
    }

    private void notifyBackKey() {
        Log.d("TapStrengthAdjustAct", "notifyBackKey");
        Intent intent = new Intent();
        intent.setAction("org.omnirom.device.NOTIFY_BACK_KEY");
        intent.setPackage("org.omnirom.device");
        sendBroadcastAsUser(intent, UserHandle.CURRENT);
    }
}
