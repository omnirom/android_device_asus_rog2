/*
 * Copyright (C) 2021 The OmniROM Project
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
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import java.util.Map;

public class ApplyFps {

    private static final String SURFACE_FLINGER_SERVICE_KEY = "SurfaceFlinger";
    private static final String SURFACE_COMPOSER_INTERFACE_KEY = "android.ui.ISurfaceComposer";
    private static final int SURFACE_FLINGER_CODE = 1034;

    private Map<Integer, Integer> fpsMap = Map.of(60, 0, 90, 1, 120, 2);
    private Context mContext;
    private IBinder mSurfaceFlinger;

    public ApplyFps(Context context) {
        mContext = context;
        mSurfaceFlinger = ServiceManager.getService(SURFACE_FLINGER_SERVICE_KEY);
    }

    protected void changeFps(SharedPreferences sharedPreferences, int fps) {
        // Display
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
        // Save it
        sharedPreferences.edit().putInt(DeviceSettings.FPS, fps).apply();
    }
}