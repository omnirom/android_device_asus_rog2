/*
* Copyright (C) 2021 The OmniROM Project
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.omnirom.device;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

public class CategoryProvider extends ContentProvider {
    public Bundle call(String str, String str2, Bundle bundle) {
        if ("getCategoryId".equals(str)) {
            boolean z = false;
            if (bundle != null) {
                z = bundle.getBoolean("original_category", false);
            }
            Bundle bundle2 = new Bundle();
            bundle2.putString("category", str2);
            return bundle2;
        } else if (!"getWeb".equals(str)) {
            return super.call(str, str2, bundle);
        } else {
            Bundle bundle3 = new Bundle();
            bundle3.putString("web", str2);
            return bundle3;
        }
    }

    public int delete(Uri uri, String str, String[] strArr) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public String getType(Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public boolean onCreate() {
        return false;
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
