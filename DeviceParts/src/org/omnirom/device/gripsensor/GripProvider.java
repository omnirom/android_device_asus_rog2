package org.omnirom.device.gripsensor;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class GripProvider extends ContentProvider {
    public static final Uri CONTENT_URI_LONG = Uri.parse("content://org.omnirom.device.gripsensor/long_grip_table");
    public static final Uri CONTENT_URI_SHORT = Uri.parse("content://org.omnirom.device.gripsensor/short_grip_table");
    public static final Uri CONTENT_URI_TAP = Uri.parse("content://org.omnirom.device.gripsensor/tap_grip_table");
    private static final UriMatcher mMatcher = new UriMatcher(-1);
    private Context mContext;
    GripDBHelper mGripDbHelper = null;

    public String getType(Uri uri) {
        return null;
    }

    static {
        mMatcher.addURI("org.omnirom.device.gripsensor", "short_grip_table", 1);
        mMatcher.addURI("org.omnirom.device.gripsensor", "long_grip_table", 2);
        mMatcher.addURI("org.omnirom.device.gripsensor", "tap_grip_table", 3);
        mMatcher.addURI("org.omnirom.device.gripsensor", "short_grip_table/#", 4);
        mMatcher.addURI("org.omnirom.device.gripsensor", "long_grip_table/#", 5);
        mMatcher.addURI("org.omnirom.device.gripsensor", "tap_grip_table/#", 6);
    }

    public boolean onCreate() {
        this.mContext = getContext();
        this.mGripDbHelper = new GripDBHelper(getContext());
        return true;
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        SQLiteQueryBuilder sQLiteQueryBuilder = new SQLiteQueryBuilder();
        String str3 = "tap_grip_table";
        switch (mMatcher.match(uri)) {
            case 1:
                sQLiteQueryBuilder.setTables("short_grip_table");
                str3 = "short_grip_table";
                break;
            case 2:
                sQLiteQueryBuilder.setTables("long_grip_table");
                str3 = "long_grip_table";
                break;
            case 3:
                sQLiteQueryBuilder.setTables(str3);
                break;
            case 4:
                sQLiteQueryBuilder.setTables("short_grip_table");
                sQLiteQueryBuilder.appendWhere("uid = " + uri.getPathSegments().get(1));
                str3 = "short_grip_table";
                break;
            case 5:
                sQLiteQueryBuilder.setTables("long_grip_table");
                sQLiteQueryBuilder.appendWhere("uid = " + uri.getPathSegments().get(1));
                str3 = "long_grip_table";
                break;
            case 6:
                sQLiteQueryBuilder.setTables(str3);
                sQLiteQueryBuilder.appendWhere("uid = " + uri.getPathSegments().get(1));
                break;
            default:
                str3 = null;
                break;
        }
        if (str3 != null) {
            return sQLiteQueryBuilder.query(this.mGripDbHelper.getReadableDatabase(), strArr, str, strArr2, null, null, "uid");
        }
        Log.d("GripProvider", "No specify table Name");
        return null;
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        Uri uri2;
        SQLiteDatabase writableDatabase = this.mGripDbHelper.getWritableDatabase();
        String tableName = getTableName(uri);
        if (tableName == null || contentValues == null) {
            Log.d("GripProvider", "No specify table Name or give values");
            return null;
        }
        long insert = writableDatabase.insert(tableName, null, contentValues);
        if (insert <= 0) {
            return null;
        }
        Log.d("GripProvider", "inset(uri=" + uri + ", values=" + contentValues.toString() + " rowId: " + insert);
        switch (mMatcher.match(uri)) {
            case 1:
            case 4:
                uri2 = CONTENT_URI_SHORT;
                break;
            case 2:
            case 5:
                uri2 = CONTENT_URI_LONG;
                break;
            case 3:
            case 6:
                uri2 = CONTENT_URI_TAP;
                break;
            default:
                uri2 = null;
                break;
        }
        if (uri2 == null) {
            return null;
        }
        Uri withAppendedId = ContentUris.withAppendedId(uri2, insert);
        this.mContext.getContentResolver().notifyChange(withAppendedId, null);
        return withAppendedId;
    }

    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        int i;
        SQLiteDatabase writableDatabase = this.mGripDbHelper.getWritableDatabase();
        String str2 = "";
        switch (mMatcher.match(uri)) {
            case 1:
                writableDatabase.update("short_grip_table", contentValues, null, strArr);
                i = 0;
                break;
            case 2:
                writableDatabase.update("long_grip_table", contentValues, null, strArr);
                i = 0;
                break;
            case 3:
                writableDatabase.update("tap_grip_table", contentValues, null, strArr);
                i = 0;
                break;
            case 4:
                StringBuilder sb = new StringBuilder();
                sb.append("uid =");
                sb.append(uri.getPathSegments().get(1));
                if (!TextUtils.isEmpty(str)) {
                    str2 = " and (" + str + ") ";
                }
                sb.append(str2);
                i = writableDatabase.update("short_grip_table", contentValues, sb.toString(), strArr);
                break;
            case 5:
                StringBuilder sb2 = new StringBuilder();
                sb2.append("uid =");
                sb2.append(uri.getPathSegments().get(1));
                if (!TextUtils.isEmpty(str)) {
                    str2 = " and (" + str + ") ";
                }
                sb2.append(str2);
                i = writableDatabase.update("long_grip_table", contentValues, sb2.toString(), strArr);
                break;
            case 6:
                StringBuilder sb3 = new StringBuilder();
                sb3.append("uid =");
                sb3.append(uri.getPathSegments().get(1));
                if (!TextUtils.isEmpty(str)) {
                    str2 = " and (" + str + ") ";
                }
                sb3.append(str2);
                i = writableDatabase.update("tap_grip_table", contentValues, sb3.toString(), strArr);
                break;
            default:
                i = 0;
                break;
        }
        this.mContext.getContentResolver().notifyChange(uri, null);
        return i;
    }

    public int delete(Uri uri, String str, String[] strArr) {
        int i;
        SQLiteDatabase writableDatabase = this.mGripDbHelper.getWritableDatabase();
        String str2 = "";
        switch (mMatcher.match(uri)) {
            case 1:
                writableDatabase.delete("short_grip_table", null, strArr);
                i = 0;
                break;
            case 2:
                writableDatabase.delete("long_grip_table", null, strArr);
                i = 0;
                break;
            case 3:
                writableDatabase.delete("tap_grip_table", null, strArr);
                i = 0;
                break;
            case 4:
                StringBuilder sb = new StringBuilder();
                sb.append("uid =");
                sb.append(uri.getPathSegments().get(1));
                if (!TextUtils.isEmpty(str)) {
                    str2 = " and (" + str + ") ";
                }
                sb.append(str2);
                i = writableDatabase.delete("short_grip_table", sb.toString(), strArr);
                break;
            case 5:
                StringBuilder sb2 = new StringBuilder();
                sb2.append("uid =");
                sb2.append(uri.getPathSegments().get(1));
                if (!TextUtils.isEmpty(str)) {
                    str2 = " and (" + str + ") ";
                }
                sb2.append(str2);
                i = writableDatabase.delete("long_grip_table", sb2.toString(), strArr);
                break;
            case 6:
                StringBuilder sb3 = new StringBuilder();
                sb3.append("uid =");
                sb3.append(uri.getPathSegments().get(1));
                if (!TextUtils.isEmpty(str)) {
                    str2 = " and (" + str + ") ";
                }
                sb3.append(str2);
                i = writableDatabase.delete("tap_grip_table", sb3.toString(), strArr);
                break;
            default:
                i = 0;
                break;
        }
        this.mContext.getContentResolver().notifyChange(uri, null);
        return i;
    }

    private String getTableName(Uri uri) {
        switch (mMatcher.match(uri)) {
            case 1:
            case 4:
                return "short_grip_table";
            case 2:
            case 5:
                return "long_grip_table";
            case 3:
            case 6:
                return "tap_grip_table";
            default:
                return null;
        }
    }
}
