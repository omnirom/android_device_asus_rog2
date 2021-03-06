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
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import androidx.preference.PreferenceManager;
import java.util.Map;

public class FrameRateTileService extends TileService {

    private static final String DEFAULT_FPS_VALUE = "60";
    private static final String FPS_VALUE_90 = "90";
    private static final String FPS_VALUE_120 = "120";
    private static final String SURFACE_FLINGER_SERVICE_KEY = "SurfaceFlinger";
    private static final String SURFACE_COMPOSER_INTERFACE_KEY = "android.ui.ISurfaceComposer";
    private static final int SURFACE_FLINGER_CODE = 1034;

    private Map<Integer, Integer> fpsMap = Map.of(60, 0, 90, 1, 120, 2);
    private Context mContext;
    private IBinder mSurfaceFlinger;

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

        String value = Settings.System.getString(this.getContentResolver(), DeviceSettings.FPS);

        getQsTile().setState(Tile.STATE_ACTIVE);
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
        getQsTile().updateTile();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
    }

    @Override
    public void onClick() {
        super.onClick();

        String value = Settings.System.getString(this.getContentResolver(), DeviceSettings.FPS);

        switch (value) {
            case DEFAULT_FPS_VALUE:
                changeFps(Integer.valueOf(FPS_VALUE_90));
                getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_refresh_rate_90));
                break;
            case FPS_VALUE_90:
                changeFps(Integer.valueOf(FPS_VALUE_120));
                getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_refresh_rate_120));
                break;
            case FPS_VALUE_120:
                changeFps(Integer.valueOf(DEFAULT_FPS_VALUE));
                getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_refresh_rate));
                break;
        }
        getQsTile().updateTile();
    }

    private void changeFps(int fps) {
        mSurfaceFlinger = ServiceManager.getService(SURFACE_FLINGER_SERVICE_KEY);
        try {
            if (mSurfaceFlinger != null) {
                final Parcel data = Parcel.obtain();
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                data.writeInterfaceToken(SURFACE_COMPOSER_INTERFACE_KEY);
                data.writeInt(fpsMap.getOrDefault(fps, -1));
                mSurfaceFlinger.transact(SURFACE_FLINGER_CODE, data, null, 0);
                data.recycle();
            }
        } catch (RemoteException ex) {
               // intentional no-op
        }
            Settings.System.putInt(this.getContentResolver(), DeviceSettings.FPS, fps);
    }
}