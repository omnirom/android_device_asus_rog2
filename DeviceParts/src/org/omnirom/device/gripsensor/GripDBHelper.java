package org.omnirom.device.gripsensor;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class GripDBHelper extends SQLiteOpenHelper {
    public GripDBHelper(Context context) {
        super(context, "gripsetting.db", (SQLiteDatabase.CursorFactory) null, 1);
    }

    public void onCreate(SQLiteDatabase sQLiteDatabase) {
        Log.d("GripDBHelper", "create db...");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS short_grip_table(uid INTEGER PRIMARY KEY, pkgName TEXT, currentMode INTEGER DEFAULT -1, sysprop TEXT, syspropValue INTERGER DEFAULT -1,broadcastPkg Text, broadcastAction Text, touchX INTEGER DEFAULT -1, touchY INTEGER DEFAULT -1, keycode INTEGER DEFAULT -1, settingType TEXT, settingName TEXT, settingValue INTERGER DEFAULT -1, launchPkg TEXT, launchCls Text, specialAction INTERGER DEFAULT -1)");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS long_grip_table(uid INTEGER PRIMARY KEY, pkgName TEXT, currentMode INTEGER DEFAULT -1, sysprop TEXT, syspropValue INTERGER DEFAULT -1,broadcastPkg Text, broadcastAction Text, touchX INTEGER DEFAULT -1, touchY INTEGER DEFAULT -1, keycode INTEGER DEFAULT -1, settingType TEXT, settingName TEXT, settingValue INTERGER DEFAULT -1, launchPkg TEXT, launchCls Text, specialAction INTERGER DEFAULT -1)");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS tap_grip_table(uid INTEGER PRIMARY KEY, pkgName TEXT, currentMode INTEGER DEFAULT -1, l1Keycode INTEGER DEFAULT -1, l1TouchX INTEGER DEFAULT -1, l1TouchY INTEGER DEFAULT -1, r1Keycode INTEGER DEFAULT -1, r1TouchX INTEGER DEFAULT -1, r1TouchY INTEGER DEFAULT -1, r2Keycode INTEGER DEFAULT -1, r2TouchX INTEGER DEFAULT -1,r2TouchY INTEGER DEFAULT -1, specialAction INTERGER DEFAULT -1)");
    }

    public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        Log.d("GripDBHelper", "modify db from version " + i + " to version " + i2 + " ...");
        sQLiteDatabase.execSQL("drop table if exists short_grip_table");
        sQLiteDatabase.execSQL("drop table if exists long_grip_table");
        sQLiteDatabase.execSQL("drop table if exists tap_grip_table");
        onCreate(sQLiteDatabase);
    }
}
