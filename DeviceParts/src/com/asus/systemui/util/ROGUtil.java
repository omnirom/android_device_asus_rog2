package com.asus.systemui.util;

import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;

public class ROGUtil {
    public static final String ASUS_LAUNCHER_ACTIVITY = "com.android.launcher3.SecondaryDisplayLauncher";
    public static final int DEFAULT_VIEW_MEMTYPE = 134217728;
    public static final int DISPLAY_MODE_240p = 1;
    public static final int DISPLAY_MODE_480p = 2;
    public static final int DISPLAY_MODE_MIRROR = 0;
    public static final int DISPLAY_MODE_NOT_SET = -1;
    public static final String DISPLAY_MODE_WITH_DOCK = "display_mode_with_dock";
    public static final String DOCK_NAME_ASUS_DT = "3";
    public static final String DOCK_NAME_ASUS_STATION = "2";
    public static final int DOCK_TYPE_ASUS_DT = 8;
    public static final int DOCK_TYPE_ASUS_GAMEVICE = 14;
    public static final int DOCK_TYPE_ASUS_INBOX = 6;
    public static final int DOCK_TYPE_ASUS_OTHER = 9;
    public static final int DOCK_TYPE_ASUS_PRODONGLE = 13;
    public static final int DOCK_TYPE_ASUS_STATION = 7;
    public static final int DOCK_TYPE_NONE = 0;
    public static final int DOCK_TYPE_STATE_PD_GV = 15;
    public static final int EXTERNAL_MASK = 128;
    public static final int EXTERNAL_VIEW_MEMTYPE = 536870912;
    public static final String EXTRA_DISPLAY_ID = "extra.DISPLAY_ID";
    public static final String EXTRA_KEYCODE = "extra.KEYCODE";
    public static final int ILLEGAL_DISPLAY_ID = -1;
    public static final int INPUT_METHOD_KEY_EXT = 865;
    public static final int INPUT_METHOD_KEY_PHONE = 864;
    public static final String LAUNCHER_PKG = "com.asus.launcher";
    public static final int NORMAL_MASK = -1;
    public static final String PERMISSION_RECEIVE_NAVIGATION_BUTTON_PRESS = "com.android.systemui.permission.RECEIVE_NAVIGATION_BUTTON_PRESS";
    public static final String SYSTEM_UI_ACTION_NAVIGATION_BUTTON_PRESS = "system.ui.action.NAVIGATION_BUTTON_PRESS";
    public static final String TAG = "ROGUtils";
    private static String sCurrentDongleName = "";
    private static int sCurrentDongleType = 0;
    public static int sCurrentInputKey = 864;
    private static int sDisplayModeForDT = 0;
    private static int sExternalDisplayId = -1;

    public static int getDongleStatus(Context context) {
        Intent registerReceiver = context.registerReceiver(null, new IntentFilter("android.intent.action.DOCK_EVENT"));
        if (registerReceiver != null) {
            return registerReceiver.getIntExtra("android.intent.extra.DOCK_STATE", 0);
        }
        return 0;
    }

    public static String getDongleName(String str) {
        if (str == null) {
            return null;
        }
        String[] split = str.split(":");
        if (split.length == 2) {
            return split[1];
        }
        return null;
    }

    public static boolean isDockAsusInbox(Context context) {
        return getDongleStatus(context) == 6;
    }

    public static boolean isDockAsusDt(Context context) {
        return getDongleStatus(context) == 8;
    }

    public static boolean isDockAsusDt() {
        return "3".equalsIgnoreCase(sCurrentDongleName);
    }

    public static boolean isDockAsusStation(Context context) {
        return getDongleStatus(context) == 7;
    }

    public static boolean isDockAsusStation() {
        return "2".equalsIgnoreCase(sCurrentDongleName);
    }

    public static void updateCurrentDongleType(int i) {
        sCurrentDongleType = i;
    }

    public static int getCurrentDongleType() {
        return sCurrentDongleType;
    }

    public static void updateCurrentDongleName(Display display) {
        sCurrentDongleName = null;
        if (display != null) {
            sCurrentDongleName = getDongleName(display.getName());
        }
    }

    public static String getCurrentDongleName() {
        return sCurrentDongleName;
    }

    public static void updateDTDisplayMode(Context context) {
        sDisplayModeForDT = Settings.Secure.getInt(context.getContentResolver(), "display_mode_with_dock", 0);
        Log.d(TAG, "setting display_mode_with_dock: " + sDisplayModeForDT);
    }

    public static boolean isDTTwinView() {
        return ("3".equals(sCurrentDongleName) || sCurrentDongleType == 8) && sDisplayModeForDT != 0;
    }

    public static boolean isDTTabletMode() {
        return ("3".equals(sCurrentDongleName) || sCurrentDongleType == 8) && sDisplayModeForDT == 1;
    }

    public static int getDTDisplayMode() {
        return sDisplayModeForDT;
    }

    public static boolean isInputDisplayOnExternalDisplay() {
        return sCurrentInputKey == 865;
    }

    public static boolean isInputDisplayOnDefaultDisplay() {
        return sCurrentInputKey == 864;
    }

    public static void addExternalDisplay(int i) {
        if (i != 0) {
            sExternalDisplayId = i;
        }
    }

    public static void removeExternalDisplay(int i) {
        if (i == sExternalDisplayId) {
            sExternalDisplayId = -1;
        }
    }

    public static int getExternalDisplayId() {
        return sExternalDisplayId;
    }

    public static void notifyAppLock(Context context, int i, int i2) {
        Log.d(TAG, "notifyAppLock: code = " + i + ", displayId = " + i2);
        Intent intent = new Intent(SYSTEM_UI_ACTION_NAVIGATION_BUTTON_PRESS);
        intent.putExtra(EXTRA_KEYCODE, i);
        intent.putExtra(EXTRA_DISPLAY_ID, i2);
        context.sendBroadcast(intent, "com.android.systemui.permission.RECEIVE_NAVIGATION_BUTTON_PRESS");
    }

    public static void launchLauncher(Context context) {
        ComponentName componentName = new ComponentName(LAUNCHER_PKG, ASUS_LAUNCHER_ACTIVITY);
        Intent intent = new Intent();
        intent.setComponent(componentName);
        intent.setPackage(LAUNCHER_PKG);
        intent.addFlags(268500992);
        ActivityOptions makeBasic = ActivityOptions.makeBasic();
        makeBasic.setLaunchDisplayId(sExternalDisplayId);
        if (isIntentActivityExist(context, intent)) {
            context.startActivity(intent, makeBasic.toBundle());
        }
        Intent intent2 = new Intent("android.intent.action.MAIN");
        intent2.addCategory("android.intent.category.HOME");
        intent2.setFlags(268435456);
        if (isIntentActivityExist(context, intent2)) {
            context.startActivity(intent2);
        }
    }

    public static boolean isIntentActivityExist(Context context, Intent intent) {
        return context.getPackageManager().queryIntentActivities(intent, 65536).size() > 0;
    }
}
