package com.asus.systemui.util;

import java.util.HashMap;
import java.util.Map;

public class AsusNavigationBarUtility {
    public static final int ACCESSIBILITY_KEY_CODE = 108;
    public static String ASUS_CAMERA_ACTIVITY = "com.asus.camera.CameraApp";
    public static String ASUS_GALLERY_ACTIVITY = "com.asus.gallery.app.EPhotoActivity";
    public static final String ASUS_TOOL_SHOW = "show";
    public static final int BACK_KEY_CODE = 4;
    public static final int CAMERA_TOOL_KEY_CODE = 110;
    public static final String CAMERA_TOOL_LAUNCH = "com.asus.motorservice.action.WIDGET_BTN_CLICKED";
    public static final String CAMERA_TOOL_PACKAGE = "com.asus.motorservice";
    public static final String EXTRA_GAME_GENIE_PANEL_EXPANDED = "expand";
    public static final String GAME_GENIE_ENABLE = "enable";
    public static final int GAME_GENIE_KEY_CODE = 105;
    public static final String GAME_GENIE_KEY_LOCK_DISABLE = "com.asus.gamewidget.app.SET_LOCK_MODE_UNLOCK";
    public static final String GAME_GENIE_KEY_LOCK_ENABLE = "com.asus.gamewidget.app.SET_LOCK_MODE_LOCK";
    public static final String GAME_GENIE_LAUNCH = "com.asus.gamewidget.action.WIDGET_BTN_CLICKED";
    public static final String GAME_GENIE_LAUNCH_SIDE = "side";
    public static final String GAME_GENIE_LOCK_TOUCH = "com.asus.gamewidget.action.LOCK_TOUCH";
    public static final String GAME_GENIE_PACKAGE = "com.asus.gamewidget";
    public static final String GAME_GENIE_PANEL_EXPANDED = "com.asus.gamewidget.action.PANEL_EXPANDED";
    public static final String GAME_GENIE_SHOW_WIDGET_BTN = "com.asus.gamewidget.action.SHOW_WIDGET_BTN";
    public static final String GAME_GENIE_STOP = "com.asus.gamewidget.app.STOP";
    public static final String GAME_GENIE_SWIPE_TWICE = "com.asus.gamewidget.action.SWIPE_TWICE";
    public static final int GESTURE_BACK = 0;
    public static final int GESTURE_ENABLE = 1;
    public static final int GESTURE_HOME = 1;
    public static final int GESTURE_RECENT = 2;
    public static final int GESTURE_TOOL = 3;
    public static final int HIDE_KEY_CODE = 101;
    public static final String HIDE_NAVI_BAR_PREF_PROVIDER_KEY = "hide_navi_bar_provider_key";
    public static final int HOME_KEY_CODE = 3;
    public static final String IMMERSIVE_MODE_TUTORIAL_ENABLE = "enable_immersive_mode_tutorial";
    public static final int LAYOUT_MODE_BACK_HOME_RECENT = 0;
    public static final int LAYOUT_MODE_RECENT_HOME_BACK = 1;
    public static final String LONG_PRESSED_FUNC = "long_pressed_func";
    public static final int LONG_PRESSED_FUNC_DEFAULT = 0;
    public static final int LONG_PRESSED_FUNC_MULTIWINDOW = 1;
    public static final int LONG_PRESSED_FUNC_RECENTLIST = 2;
    public static final int LONG_PRESSED_FUNC_SCREENSHOT = 0;
    public static final int MENU_KEY_CODE = 82;
    public static final String NAVIGATION_BAR_VISIBLE_CONTROL = "nav_vis_ctrl";
    public static final String NAVIGATION_KEY_LOC_PROVIDER_KEY = "button_layout_provider_key";
    public static final int NAV_BAR_MODE_ASUS_GESTURE = 4;
    public static final int NON = -1;
    public static final Map<Integer, String> NaviBtnKeyCodeMap = new HashMap<Integer, String>() {
        {
            put(3, "Home");
            put(4, "Back");
            put(100, "Recents");
            put(101, "Hide_navi_bar");
            put(102, "Pin_Hide_navi_bar");
            put(104, "PageMarker");
            put(105, "GameGenie");
            put(106, "SelfieMaser");
            put(107, "ZeniMoji");
            put(108, "Accessibility");
            put(109, "Rotation");
        }
    };
    public static final int PAGE_MARKER_KEY_CODE = 104;
    public static final String PAGE_MARKER_LAUNCH = "com.asus.browsergenie.action.WIDGET_BTN_CLICKED";
    public static final String PAGE_MARKER_PACKAGE = "com.asus.browsergenie";
    public static final int PIN_KEY_CODE = 102;
    public static final int RECENTS_KEY_CODE = 100;
    public static final int ROTATE_KEY_CODE = 109;
    public static final int SCALING_KEY_CODE = 103;
    public static final int SELFIE_MASTER_KEY_CODE = 106;
    public static final String SELFIE_MASTER_LAUNCH = "com.asus.selfiemaster.action.WIDGET_BTN_CLICKED";
    public static final String SELFIE_MASTER_PACKAGE = "com.asus.selfiemaster";
    public static final String SETTINGS_HIDE_NAVIGATION_BAR_KEY = "nav_vis_ctrl";
    public static final String SETTINGS_NAVIGATION_BAR_GESTURE_KEY = "navi_gesture_provider_key";
    public static final String SPECIFIC_INCALL_APP = "asus.intent.action.GAME_DND_CUSTOM_ACTIVITY";
    public static final int ZENI_MOJI_KEY_CODE = 107;
    public static final String ZENI_MOJI_LAUNCH = "com.asus.zenimoji.action.WIDGET_BTN_CLICKED";
    public static final String ZENI_MOJI_PACKAGE = "com.asus.zenimoji";
    private static boolean sAsusGestureEnable = false;
    private static boolean sImmersiveTutorialEnable = false;
    private static int sLayoutMode = 0;
    private static int sNaviBarMode = 0;
    private static int sRecentsLongPressFunc = 0;

    public static void setNaviBarLayoutMode(int i) {
        sLayoutMode = i;
    }

    public static int getNaviBarLayoutMode() {
        return sLayoutMode;
    }

    public static void setRecentsLongPressFunc(int i) {
        sRecentsLongPressFunc = i;
    }

    public static int getRecentsLongPressFunc() {
        return sRecentsLongPressFunc;
    }

    public static void updateAsusGestureStatus(boolean z) {
        sAsusGestureEnable = z;
    }

    public static boolean isAsusGestureEnable() {
        return sAsusGestureEnable;
    }

    public static void updateNaviBarMode(int i) {
        sNaviBarMode = i;
    }

    public static int getNaviBarMode() {
        return sNaviBarMode;
    }

    public static void setImmersiveTutorialStatus(boolean z) {
        sImmersiveTutorialEnable = z;
    }

    public static boolean isImmersiveTutorialEnable() {
        return sImmersiveTutorialEnable;
    }
}
