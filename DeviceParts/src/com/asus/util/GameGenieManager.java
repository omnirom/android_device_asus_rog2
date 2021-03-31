package com.asus.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import org.omnirom.device.R;
import com.asus.systemui.util.AsusNavigationBarUtility;
import com.asus.systemui.util.GameSpaceUtil;
import com.asus.systemui.util.ROGUtil;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GameGenieManager {
    private static boolean sEnableTriggerGameGenie = false;
    private static boolean sIsGameGenieActive = false;
    private static boolean sIsKeyLockEnable = false;
    private static boolean sIsTouchLockEnable = false;
    private static boolean sPanelExpand = false;
    private static boolean sSwipeLockEnable = false;
    private final String TAG = getClass().getSimpleName();
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                char c = 65535;
                boolean z = true;
                switch (action.hashCode()) {
                    case -1376498410:
                        if (action.equals(AsusNavigationBarUtility.GAME_GENIE_STOP)) {
                            c = 4;
                            break;
                        }
                        break;
                    case -836419524:
                        if (action.equals(AsusNavigationBarUtility.GAME_GENIE_LOCK_TOUCH)) {
                            c = 1;
                            break;
                        }
                        break;
                    case -541430638:
                        if (action.equals(AsusNavigationBarUtility.GAME_GENIE_SWIPE_TWICE)) {
                            c = 6;
                            break;
                        }
                        break;
                    case -205872795:
                        if (action.equals(AsusNavigationBarUtility.GAME_GENIE_PANEL_EXPANDED)) {
                            c = 5;
                            break;
                        }
                        break;
                    case -35484700:
                        if (action.equals(AsusNavigationBarUtility.GAME_GENIE_KEY_LOCK_ENABLE)) {
                            c = 2;
                            break;
                        }
                        break;
                    case 8454578:
                        if (action.equals(AsusNavigationBarUtility.GAME_GENIE_SHOW_WIDGET_BTN)) {
                            c = 0;
                            break;
                        }
                        break;
                    case 515954621:
                        if (action.equals(AsusNavigationBarUtility.GAME_GENIE_KEY_LOCK_DISABLE)) {
                            c = 3;
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                        boolean unused = GameGenieManager.sIsGameGenieActive = intent.getBooleanExtra(AsusNavigationBarUtility.ASUS_TOOL_SHOW, false);
                        if (!GameGenieManager.sIsGameGenieActive || !intent.getBooleanExtra(AsusNavigationBarUtility.GAME_GENIE_ENABLE, true)) {
                            z = false;
                        }
                        boolean unused2 = GameGenieManager.sEnableTriggerGameGenie = z;
                        Log.d(GameGenieManager.this.TAG, "[BroadcastReceiver] GAME_GENIE_SHOW_WIDGET_BTN GameGenieActive: " + GameGenieManager.sIsGameGenieActive + " EnableTrigger: " + GameGenieManager.sEnableTriggerGameGenie);
                        if (!GameGenieManager.sIsGameGenieActive) {
                            boolean unused3 = GameGenieManager.sIsKeyLockEnable = false;
                            break;
                        }
                        break;
                    case 1:
                        Log.d(GameGenieManager.this.TAG, "[BroadcastReceiver] GAME_GENIE_LOCK_TOUCH");
                        boolean unused4 = GameGenieManager.sIsTouchLockEnable = intent.getBooleanExtra(AsusNavigationBarUtility.GAME_GENIE_ENABLE, false);
                        break;
                    case 2:
                        Log.d(GameGenieManager.this.TAG, "[BroadcastReceiver] GAME_GENIE_KEY_LOCK_ENABLE");
                        boolean unused5 = GameGenieManager.sIsKeyLockEnable = true;
                        break;
                    case 3:
                        Log.d(GameGenieManager.this.TAG, "[BroadcastReceiver] GAME_GENIE_KEY_LOCK_DISABLE");
                        boolean unused6 = GameGenieManager.sIsKeyLockEnable = false;
                        break;
                    case 4:
                        Log.d(GameGenieManager.this.TAG, "[BroadcastReceiver] GAME_GENIE_STOP");
                        boolean unused7 = GameGenieManager.sIsKeyLockEnable = false;
                        break;
                    case 5:
                        boolean unused8 = GameGenieManager.sPanelExpand = intent.getBooleanExtra(AsusNavigationBarUtility.EXTRA_GAME_GENIE_PANEL_EXPANDED, false);
                        Log.d(GameGenieManager.this.TAG, "[BroadcastReceiver] GAME_GENIE_LOCK_TOUCH " + GameGenieManager.sPanelExpand);
                        break;
                    case 6:
                        boolean unused9 = GameGenieManager.sSwipeLockEnable = intent.getBooleanExtra(AsusNavigationBarUtility.GAME_GENIE_ENABLE, false);
                        Log.d(GameGenieManager.this.TAG, "[BroadcastReceiver] GAME_GENIE_SWIPE_TWICE " + GameGenieManager.sSwipeLockEnable);
                        break;
                }
                for (GameGenieCallback gameGenieCallback : GameGenieManager.this.mCallbacks) {
                    gameGenieCallback.onGameGenieStatusChanged();
                }
            }
        }
    };
    private List<GameGenieCallback> mCallbacks = new ArrayList();
    private Context mContext;
    private final ContentObserver mGameSpaceObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
        public void onChange(boolean z) {
            GameSpaceUtil.updateStatus(GameGenieManager.this.mContext);
            String str = GameGenieManager.this.TAG;
            Log.d(str, "[GameSpaceObserver] enable: " + GameSpaceUtil.isGameSpaceEnable() + " onPhone: " + GameSpaceUtil.isOnPhone());
            LocalBroadcastManager.getInstance(GameGenieManager.this.mContext).sendBroadcast(new Intent(GameSpaceUtil.GAME_SPACE_UPDATE_ICON));
        }
    };

    public interface GameGenieCallback {
        default void onGameGenieStatusChanged() {
        }
    }

    @Inject
    public GameGenieManager(Context context) {
        this.mContext = context;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AsusNavigationBarUtility.GAME_GENIE_SHOW_WIDGET_BTN);
        intentFilter.addAction(AsusNavigationBarUtility.GAME_GENIE_LOCK_TOUCH);
        intentFilter.addAction(AsusNavigationBarUtility.GAME_GENIE_PANEL_EXPANDED);
        intentFilter.addAction(AsusNavigationBarUtility.GAME_GENIE_KEY_LOCK_ENABLE);
        intentFilter.addAction(AsusNavigationBarUtility.GAME_GENIE_KEY_LOCK_DISABLE);
        intentFilter.addAction(AsusNavigationBarUtility.GAME_GENIE_STOP);
        intentFilter.addAction(AsusNavigationBarUtility.GAME_GENIE_SWIPE_TWICE);
        context.registerReceiver(this.mBroadcastReceiver, intentFilter);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(GameSpaceUtil.GAME_SPACE_DISPLAY_STATE), true, this.mGameSpaceObserver);
        this.mGameSpaceObserver.onChange(true);
    }

    public void addCallback(@NonNull GameGenieCallback gameGenieCallback) {
        this.mCallbacks.add(gameGenieCallback);
    }

    public void removeCallback(@NonNull GameGenieCallback gameGenieCallback) {
        this.mCallbacks.remove(gameGenieCallback);
    }

    public static boolean isGameGenieActive() {
        return sIsGameGenieActive;
    }

    public static boolean enableTriggerGameGenie() {
        return sEnableTriggerGameGenie;
    }

    public static boolean isTouchLock() {
        return sIsTouchLockEnable;
    }

    public static boolean isPanelExpand() {
        return sPanelExpand;
    }

    public static void setGameGenieExpand(boolean z) {
        sPanelExpand = z;
    }

    public static boolean isKeyLock() {
        return sIsKeyLockEnable;
    }

    public static boolean isSwipeLockEnable() {
        return sSwipeLockEnable;
    }

    public void postKeyLockToast() {
        Context context = this.mContext;
        Toast.makeText(context, context.getString(R.string.gamegenie_lock_mode_toast), 0).show();
    }

    public static boolean shouldDisplayGameGenie(boolean z, int i) {
        if (z) {
            int currentDongleType = ROGUtil.getCurrentDongleType();
            if (currentDongleType == 0 || currentDongleType == 7 || currentDongleType != 8) {
                return false;
            }
            return enableTriggerGameGenie() && i != 0;
        }
        int currentDongleType2 = ROGUtil.getCurrentDongleType();
        if (currentDongleType2 == 0) {
            return enableTriggerGameGenie();
        }
        if (currentDongleType2 == 7) {
            return enableTriggerGameGenie() && i == 0;
        }
        if (currentDongleType2 != 8) {
            return enableTriggerGameGenie();
        }
        if (ROGUtil.isDTTwinView() || ROGUtil.isDTTabletMode()) {
            return false;
        }
        return enableTriggerGameGenie();
    }
}
