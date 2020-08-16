/*
 * Copyright (c) 2021 The OmniRom Project
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

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;
import java.util.ArrayList;

public class ArmouryCrateBroadcastReceiver extends BroadcastReceiver {
    static final IntentFilter HOME_FILTER = new IntentFilter("android.intent.action.MAIN");
    private static final String TAG = "ArmouryCrateBroadcastReceiver";

    static {
        HOME_FILTER.addCategory("android.intent.category.HOME");
        HOME_FILTER.addCategory("android.intent.category.DEFAULT");
    }

    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("com.asus.gamecenter.mode_changed")) {
            Log.d(TAG, "onReceive: " + intent.getAction());
            boolean z = true;
            if (intent.getIntExtra("ROG_GAME_SPACE_DISPLAY_STATE", 0) != 1) {
                z = false;
            }
            String stringExtra = intent.getStringExtra("previous_launcher");
            if (z) {
                stringExtra = "com.asus.gamecenter/com.asus.gamecenter.GameCenterActivity";
            } else if (stringExtra == null || stringExtra.trim().isEmpty()) {
                stringExtra = "com.asus.launcher/com.android.launcher3.Launcher";
            } else {
                Log.d(TAG, "previous launcher is " + stringExtra);
            }
            Log.d(TAG, "key: " + stringExtra);
            PackageManager packageManager = context.getPackageManager();
            ComponentName unflattenFromString = ComponentName.unflattenFromString(stringExtra);
            ArrayList<ResolveInfo> arrayList = new ArrayList();
            packageManager.getHomeActivities(arrayList);
            ArrayList arrayList2 = new ArrayList();
            for (ResolveInfo resolveInfo : arrayList) {
                ActivityInfo activityInfo = resolveInfo.activityInfo;
                arrayList2.add(new ComponentName(activityInfo.packageName, activityInfo.name));
            }
            packageManager.replacePreferredActivity(HOME_FILTER, 1048576, (ComponentName[]) arrayList2.toArray(new ComponentName[0]), unflattenFromString);
            if (!"com.asus.gamecenter/com.asus.gamecenter.GameCenterActivity".equals(stringExtra)) {
                Intent intent2 = new Intent("android.intent.action.MAIN");
                intent2.addCategory("android.intent.category.HOME");
                intent2.setFlags(268435456);
                context.startActivity(intent2);
            }
        }
    }
}
