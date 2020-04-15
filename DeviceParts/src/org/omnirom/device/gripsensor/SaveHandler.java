package org.omnirom.device.gripsensor;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import org.omnirom.device.gripsensor.SqueezeFragment;

public class SaveHandler extends Handler {
    private Context mContext;

    public void setContext(Context context) {
        if (this.mContext == null) {
            this.mContext = context;
        }
    }

    public SaveHandler(Looper looper) {
        super(looper);
    }

    public void handleMessage(Message message) {
        super.handleMessage(message);
        if (message.what == 6000) {
            saveToDb((SqueezeFragment.AppData) message.obj);
        }
    }

    private void saveToDb(SqueezeFragment.AppData appData) {
        boolean z;
        boolean z2;
        ContentResolver contentResolver = this.mContext.getContentResolver();
        String str = appData.mPath;
        Uri uri = appData.mUri;
        int i = appData.mUid;
        String str2 = appData.mPkg;
        String str3 = appData.mField;
        boolean z3 = appData.mEnable;
        boolean z4 = appData.mSaveLock;
        String[] split = str.split("/");
        String str4 = split[0];
        GripUtils.rmGripData(uri, contentResolver, i);
        if (i != 3) {
            GripUtils.rmGripData(uri, contentResolver, 1);
            z = z4;
            z2 = z3;
        } else {
            z2 = false;
            z = false;
        }
        if (str4.equals("launch")) {
            GripUtils.addOrUpdateGripData(uri, contentResolver, i, str2, z2 ? 0 : -1, null, -1, null, null, -1, -1, -1, split.length > 3 ? split[3] : null, null, -1, split[1], split[2], -1);
            if (z) {
                GripUtils.addOrUpdateGripData(uri, contentResolver, 1, "Global_grip_locked", z2 ? 0 : -1, null, -1, null, null, -1, -1, -1, split.length > 3 ? split[3] : null, null, -1, split[1], split[2], -1);
            }
            if (!z2) {
                AirTriggerUtils.setSettingsProviderForGrip(contentResolver, str3, String.valueOf(0));
            }
        } else if (str4.equals("setting")) {
            GripUtils.addOrUpdateGripData(uri, contentResolver, i, str2, z2 ? 5 : -1, null, -1, null, null, -1, -1, -1, split[1], split[2], 1, null, null, -1);
            if (z) {
                GripUtils.addOrUpdateGripData(uri, contentResolver, 1, "Global_grip_locked", z2 ? 5 : -1, null, -1, null, null, -1, -1, -1, split[1], split[2], 1, null, null, -1);
            }
            if (!z2) {
                AirTriggerUtils.setSettingsProviderForGrip(contentResolver, str3, String.valueOf(5));
            }
        } else if (str4.equals("sysprop")) {
            GripUtils.addOrUpdateGripData(uri, contentResolver, i, str2, z2 ? 1 : -1, split[1], 1, null, null, -1, -1, -1, null, null, -1, null, null, -1);
            if (z) {
                GripUtils.addOrUpdateGripData(uri, contentResolver, 1, "Global_grip_locked", z2 ? 1 : -1, split[1], 1, null, null, -1, -1, -1, null, null, -1, null, null, -1);
            }
            if (!z2) {
                AirTriggerUtils.setSettingsProviderForGrip(contentResolver, str3, String.valueOf(1));
            }
        } else if (str4.equals("broadcast")) {
            GripUtils.addOrUpdateGripData(uri, contentResolver, i, str2, z2 ? 2 : -1, null, -1, split[1], split[2], -1, -1, -1, null, null, -1, null, null, -1);
            if (z) {
                GripUtils.addOrUpdateGripData(uri, contentResolver, 1, "Global_grip_locked", z2 ? 2 : -1, null, -1, split[1], split[2], -1, -1, -1, null, null, -1, null, null, -1);
            }
            if (!z2) {
                AirTriggerUtils.setSettingsProviderForGrip(contentResolver, str3, String.valueOf(2));
            }
        } else if (str4.equals("touch")) {
            GripUtils.addOrUpdateGripData(uri, contentResolver, i, str2, z2 ? 3 : -1, null, -1, null, null, Integer.parseInt(split[1]), Integer.parseInt(split[2]), -1, null, null, -1, null, null, -1);
            if (z) {
                GripUtils.addOrUpdateGripData(uri, contentResolver, 1, "Global_grip_locked", z2 ? 3 : -1, null, -1, null, null, Integer.parseInt(split[1]), Integer.parseInt(split[2]), -1, null, null, -1, null, null, -1);
            }
            if (!z2) {
                AirTriggerUtils.setSettingsProviderForGrip(contentResolver, str3, String.valueOf(3));
            }
        } else if (str4.equals("keycode")) {
            GripUtils.addOrUpdateGripData(uri, contentResolver, i, str2, z2 ? 4 : -1, null, -1, null, null, -1, -1, Integer.parseInt(split[1]), null, null, -1, null, null, -1);
            if (z) {
                GripUtils.addOrUpdateGripData(uri, contentResolver, 1, "Global_grip_locked", z2 ? 4 : -1, null, -1, null, null, -1, -1, Integer.parseInt(split[1]), null, null, -1, null, null, -1);
            }
            if (!z2) {
                AirTriggerUtils.setSettingsProviderForGrip(contentResolver, str3, String.valueOf(4));
            }
        } else if (str4.equals("special")) {
            GripUtils.addOrUpdateGripData(uri, contentResolver, i, str2, z2 ? 6 : -1, null, -1, null, null, -1, -1, -1, null, null, -1, null, null, Integer.parseInt(split[1]));
            if (z) {
                GripUtils.addOrUpdateGripData(uri, contentResolver, 1, "Global_grip_locked", z2 ? 6 : -1, null, -1, null, null, -1, -1, -1, null, null, -1, null, null, Integer.parseInt(split[1]));
            }
            if (!z2) {
                AirTriggerUtils.setSettingsProviderForGrip(contentResolver, str3, String.valueOf(6));
            }
        }
    }
}
