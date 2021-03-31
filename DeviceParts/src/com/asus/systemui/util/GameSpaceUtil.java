package com.asus.systemui.util;

import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

public class GameSpaceUtil {
    private static final int DISABLE = 0;
    public static final String GAME_SPACE_DISPLAY_ID = "DISPLAY_ID";
    public static final String GAME_SPACE_DISPLAY_STATE = "ROG_GAME_SPACE_DISPLAY_STATE";
    private static final String GAME_SPACE_NAME = "com.asus.gamecenter.GameCenterActivity";
    private static final String GAME_SPACE_PKG = "com.asus.gamecenter";
    public static final String GAME_SPACE_UPDATE_ICON = "com.asus.rog.GAME_SPACE_UPDATE_ICON";
    private static final int ON_EXRA = 2;
    private static final int ON_PHONE = 1;
    private static final String TAG = "GameSpaceUtil";
    private static boolean sGameSpaceEnable = false;
    private static boolean sGameSpaceOnPhone = false;

    public static void updateStatus(Context context) {
        boolean z = false;
        int i = Settings.System.getInt(context.getContentResolver(), GAME_SPACE_DISPLAY_STATE, 0);
        Log.d(TAG, "[updateStatus] status: " + i);
        sGameSpaceEnable = i != 0;
        if (i == 1) {
            z = true;
        }
        sGameSpaceOnPhone = z;
    }

    public static boolean isGameSpaceEnable() {
        return sGameSpaceEnable;
    }

    public static boolean isOnPhone() {
        return sGameSpaceOnPhone;
    }

    public static boolean shouldUseGameSpaceUI(int i) {
        return sGameSpaceEnable && ((sGameSpaceOnPhone && i == 0) || (!sGameSpaceOnPhone && i != 0));
    }

    public static void launchGameSpace(Context context) {
        ComponentName componentName = new ComponentName(GAME_SPACE_PKG, GAME_SPACE_NAME);
        Intent intent = new Intent();
        intent.setComponent(componentName);
        intent.addFlags(268500992);
        ActivityOptions makeBasic = ActivityOptions.makeBasic();
        makeBasic.setLaunchDisplayId(context.getDisplayId());
        if (ROGUtil.isIntentActivityExist(context, intent)) {
            Log.d(TAG, "Launch GameSpace");
            context.startActivity(intent, makeBasic.toBundle());
            return;
        }
        Log.d(TAG, "GameSpace intent activity not exist");
    }
}
