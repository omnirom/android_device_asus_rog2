/*
 * Copyright (C) 2020 The OmniROM Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.omnirom.device;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import androidx.preference.PreferenceManager;

public class FrameRateTileService extends TileService {

    private static final String DEFAULT_FPS_VALUE = "60";
    private static final String FPS_VALUE_90 = "90";
    private static final String FPS_VALUE_120 = "120";

    private static final int DEFAULT_FPS_INT = 60;
    private static final int FPS_INT_90 = 90;
    private static final int FPS_INT_120 = 120;
    private static String OMNIFPS = "OmniFrameRateTile";
    private MakeFps mMakeFps;

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onTileAdded() {
        super.onTileAdded();
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();

        String value = Settings.System.getString(this.getContentResolver(), DeviceSettings.TEMP_FPS);
        switch (value) {
            case DEFAULT_FPS_VALUE:
                getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_refresh_rate));
                break;
            case FPS_VALUE_90:
                getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_refresh_rate_90));
                break;
            case FPS_VALUE_120:
                getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_refresh_rate_120));
                break;
        }
        getQsTile().setState(Tile.STATE_ACTIVE);
        getQsTile().updateTile();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
    }

    @Override
    public void onClick() {
        super.onClick();

        String value = Settings.System.getString(this.getContentResolver(), DeviceSettings.TEMP_FPS);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        switch (value) {
            case DEFAULT_FPS_VALUE:
                Settings.System.putString(this.getContentResolver(), DeviceSettings.TEMP_FPS, FPS_VALUE_90);
                mMakeFps.changeFps(sharedPrefs, FPS_INT_90);
                getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_refresh_rate_90));
                break;
            case FPS_VALUE_90:
                Settings.System.putString(this.getContentResolver(), DeviceSettings.TEMP_FPS, FPS_VALUE_120);
                mMakeFps.changeFps(sharedPrefs, FPS_INT_120);
                getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_refresh_rate_120));
                break;
            case FPS_VALUE_120:
                Settings.System.putString(this.getContentResolver(), DeviceSettings.TEMP_FPS, DEFAULT_FPS_VALUE);
                mMakeFps.changeFps(sharedPrefs, DEFAULT_FPS_INT);
                getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_refresh_rate));
                break;
        }
        getQsTile().updateTile();
    }
}