package org.omnirom.device.gripsensor;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class GripUtils {
    public static final String[] GRIP_ALL_COLUMN = {"uid", "pkgName", "currentMode", "sysprop", "syspropValue", "broadcastPkg", "broadcastAction", "touchX ", "touchY", "keycode", "settingType", "settingName", "settingValue", "launchPkg", "launchCls", "specialAction"};
    public static final String[] TAP_ALL_COLUMN = {"uid", "pkgName", "currentMode", "l1Keycode", "l1TouchX", "l1TouchY", "r1Keycode", "r1TouchX ", "r1TouchY", "r2Keycode", "r2TouchX", "r2TouchY", "specialAction"};
    public static final Uri[] URI_LIST = {GripProvider.CONTENT_URI_SHORT, GripProvider.CONTENT_URI_LONG};
    public static final Uri URI_LONG_GRIP = GripProvider.CONTENT_URI_LONG;
    public static final Uri URI_SHORT_GRIP = GripProvider.CONTENT_URI_SHORT;
    public static final Uri URI_TAP = GripProvider.CONTENT_URI_TAP;

    public static void addOrUpdateGripData(Uri uri, ContentResolver contentResolver, int i, String str, int i2, String str2, int i3, String str3, String str4, int i4, int i5, int i6, String str5, String str6, int i7, String str7, String str8, int i8) {
        if (i >= 0) {
            ContentValues newGripContentValue = newGripContentValue(i, str, i2, str2, i3, str3, str4, i4, i5, i6, str5, str6, i7, str7, str8, i8);
            Uri withAppendedId = ContentUris.withAppendedId(uri, (long) i);
            Cursor query = contentResolver.query(withAppendedId, GRIP_ALL_COLUMN, null, null, null);
            if (query == null || query.getCount() <= 0) {
                contentResolver.insert(uri, newGripContentValue);
                return;
            }
            do {
            } while (query.moveToNext());
            query.close();
            contentResolver.update(withAppendedId, newGripContentValue, null, null);
        }
    }

    public static GripData readGripData(Uri uri, ContentResolver contentResolver, int i) {
        GripData gripData = null;
        if (i < 0) {
            return null;
        }
        Uri uri2 = uri;
        Uri withAppendedId = ContentUris.withAppendedId(uri2, (long) i);
        if (i != 0) {
            uri2 = withAppendedId;
        }
        Cursor query = contentResolver.query(uri2, GRIP_ALL_COLUMN, null, null, null);
        if (query != null && query.getCount() > 0) {
            while (query.moveToNext()) {
                if (query.getCount() == 1 && query.getColumnCount() == 16) {
                    gripData = new GripData(query.getInt(0), query.getString(1), query.getInt(2), query.getString(3), query.getInt(4), query.getString(5), query.getString(6), query.getInt(7), query.getInt(8), query.getInt(9), query.getString(10), query.getString(11), query.getInt(12), query.getString(13), query.getString(14), query.getInt(15));
                }
            }
            query.close();
        }
        return gripData;
    }

    public static void rmGripData(Uri uri, ContentResolver contentResolver, int i) {
        if (i >= 0) {
            Uri withAppendedId = ContentUris.withAppendedId(uri, (long) i);
            Cursor query = contentResolver.query(withAppendedId, GRIP_ALL_COLUMN, null, null, null);
            if (query != null && query.getCount() > 0) {
                do {
                } while (query.moveToNext());
                query.close();
                contentResolver.delete(withAppendedId, null, null);
            }
        }
    }

    public static ContentValues newGripContentValue(int i, String str, int i2, String str2, int i3, String str3, String str4, int i4, int i5, int i6, String str5, String str6, int i7, String str7, String str8, int i8) {
        ContentValues contentValues = new ContentValues();
        if (i >= 0) {
            contentValues.put("uid", Integer.valueOf(i));
        }
        if (str != null) {
            contentValues.put("pkgName", str);
        }
        if (i2 >= 0) {
            contentValues.put("currentMode", Integer.valueOf(i2));
        }
        if (str2 != null) {
            contentValues.put("sysprop", str2);
        }
        if (i3 >= 0) {
            contentValues.put("syspropValue", Integer.valueOf(i3));
        }
        if (str3 != null) {
            contentValues.put("broadcastPkg", str3);
        }
        if (str4 != null) {
            contentValues.put("broadcastAction", str4);
        }
        if (i4 >= 0) {
            contentValues.put("touchX", Integer.valueOf(i4));
        }
        if (i5 >= 0) {
            contentValues.put("touchY", Integer.valueOf(i5));
        }
        if (i6 >= 0) {
            contentValues.put("keycode", Integer.valueOf(i6));
        }
        if (str5 != null) {
            contentValues.put("settingType", str5);
        }
        if (str6 != null) {
            contentValues.put("settingName", str6);
        }
        if (i7 >= 0) {
            contentValues.put("settingValue", Integer.valueOf(i7));
        }
        if (str7 != null) {
            contentValues.put("launchPkg", str7);
        }
        if (str8 != null) {
            contentValues.put("launchCls", str8);
        }
        if (i8 >= 0) {
            contentValues.put("specialAction", Integer.valueOf(i8));
        }
        return contentValues;
    }

    public static final class GripData {
        public String mBroadcastAction;
        public String mBroadcastPkg;
        public int mCurrentMode;
        public int mKeycode;
        public String mLaunchCls;
        public String mLaunchPkg;
        public String mPkgName;
        public String mSettingName;
        public String mSettingType;
        public int mSettingValue;
        public int mSpecialAction;
        public String mSysprop;
        public int mSyspropValue;
        public int mTouchX;
        public int mTouchY;
        public int mUID;

        public GripData(int i, String str, int i2, String str2, int i3, String str3, String str4, int i4, int i5, int i6, String str5, String str6, int i7, String str7, String str8, int i8) {
            this.mUID = i;
            if (str != null) {
                this.mPkgName = new String(str);
            }
            this.mCurrentMode = i2;
            if (str2 != null) {
                this.mSysprop = new String(str2);
            }
            this.mSyspropValue = i3;
            if (str3 != null) {
                this.mBroadcastPkg = new String(str3);
            }
            if (str4 != null) {
                this.mBroadcastAction = new String(str4);
            }
            this.mTouchX = i4;
            this.mTouchY = i5;
            this.mKeycode = i6;
            if (str5 != null) {
                this.mSettingType = new String(str5);
            }
            if (str6 != null) {
                this.mSettingName = new String(str6);
            }
            this.mSettingValue = i7;
            if (str7 != null) {
                this.mLaunchPkg = new String(str7);
            }
            if (str8 != null) {
                this.mLaunchCls = new String(str8);
            }
            this.mSpecialAction = i8;
        }
    }
}
