package org.omnirom.device.gripsensorservice;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.provider.Settings;
import android.service.quicksettings.TileService;

public abstract class DevelopmentTiles extends TileService {
    protected abstract boolean isEnabled();
    protected abstract void setIsEnabled(boolean z);

    public void onStartListening() {
        super.onStartListening();
        refresh();
    }

    public void refresh() {
        getQsTile().setState(isEnabled() ? 2 : 1);
        getQsTile().updateTile();
    }

    public void onClick() {
        boolean z = true;
        if (getQsTile().getState() != 1) {
            z = false;
        }
        setIsEnabled(z);
        refresh();
    }

    public static class SwitchTile extends DevelopmentTiles {
        private Context mContext;
        private SettingObserver mObserver;

        public void onCreate() {
            super.onCreate();
            this.mContext = getApplicationContext();
        }

        @Override
        public void onStartListening() {
            super.onStartListening();
            this.mObserver = new SettingObserver("air_trigger_enable") {
                public void onChange(boolean z, Uri uri) {
                    SwitchTile.this.refresh();
                }
            };
            ContentResolver contentResolver = this.mContext.getContentResolver();
            SettingObserver settingObserver = this.mObserver;
            contentResolver.registerContentObserver(settingObserver.uri, false, settingObserver);
        }

        public void onStopListening() {
            this.mContext.getContentResolver().unregisterContentObserver(this.mObserver);
        }

        @Override
        protected boolean isEnabled() {
            return Settings.Global.getInt(this.mContext.getContentResolver(), "air_trigger_enable", 0) == 1;
        }

        @Override
        protected void setIsEnabled(boolean z) {
            Intent intent = new Intent();
            intent.setAction(z ? "org.omnirom.device.SYSTEMUI_AIR_TRIGGER_ON" : "org.omnirom.device.SYSTEMUI_AIR_TRIGGER_OFF");
            intent.setPackage("org.omnirom.device");
            this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
        }
    }

    private static class SettingObserver extends ContentObserver {
        public final Uri uri;

        public SettingObserver(String str) {
            super(new Handler(Looper.getMainLooper()));
            this.uri = Settings.Global.getUriFor(str);
        }
    }
}
