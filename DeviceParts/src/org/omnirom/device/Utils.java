/*
* Copyright (C) 2013 The OmniROM Project
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

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.SystemProperties;
import android.util.Log;
import android.widget.SeekBar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class Utils {

    /**
     * Write a string value to the specified file.
     * @param filename      The filename
     * @param value         The value
     */
    public static void writeValue(String filename, String value) {
        if (filename == null) {
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(new File(filename));
            fos.write(value.getBytes());
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if the specified file exists.
     * @param filename      The filename
     * @return              Whether the file exists or not
     */
    public static boolean fileExists(String filename) {
        if (filename == null) {
            return false;
        }
        return new File(filename).exists();
    }

    public static boolean fileWritable(String filename) {
        return fileExists(filename) && new File(filename).canWrite();
    }

    public static String readLine(String filename) {
        if (filename == null) {
            return null;
        }
        BufferedReader br = null;
        String line = null;
        try {
            br = new BufferedReader(new FileReader(filename), 1024);
            line = br.readLine();
        } catch (IOException e) {
            return null;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return line;
    }

    public static boolean getFileValueAsBoolean(String filename, boolean defValue) {
        String fileValue = readLine(filename);
        if(fileValue!=null){
            return (fileValue.equals("0")?false:true);
        }
        return defValue;
    }

    public static boolean getLineValueAsBoolean(String filename, boolean defValue) {
        String fileValue = readLine(filename);
        if(fileValue!=null){
            return (fileValue.equals("Glove Mode: Off")?false:true);
        }
        return defValue;
    }

    public static String getFileValue(String filename, String defValue) {
        String fileValue = readLine(filename);
        if(fileValue!=null){
            return fileValue;
        }
        return defValue;
    }

    /**
     * Writes the given value into the given file
     *
     * @return true on success, false on failure
     */
    public static boolean writeLine(String fileName, String value) {
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(value);
        } catch (FileNotFoundException e) {
            Log.w("Utils", "No such file " + fileName + " for writing", e);
            return false;
        } catch (IOException e) {
            Log.e("Utils", "Could not write to file " + fileName, e);
            return false;
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                // Ignored, not much we can do anyway
            }
        }

        return true;
    }

    public static boolean isCNSKU() {
        String str = SystemProperties.get("ro.build.asus.sku", "");
        return str.toLowerCase().startsWith("bby") || str.toLowerCase().startsWith("cn");
    }

    public static int dockingType(Context context) {
        Intent registerReceiver;
        if (context == null || (registerReceiver = context.registerReceiver(null, new IntentFilter("android.intent.action.DOCK_EVENT"))) == null) {
            return 0;
        }
        return registerReceiver.getIntExtra("android.intent.extra.DOCK_STATE", -1);
    }

    public static void tintSeekbar(Context context, SeekBar seekBar, int i) {
        if (seekBar != null) {
            seekBar.getThumb().setColorFilter(i, PorterDuff.Mode.SRC_IN);
            seekBar.setProgressTintList(ColorStateList.valueOf(i));
            ColorStateList valueOf = ColorStateList.valueOf(-7829368);
            //~ if (isLightStatusBar(context)) {
            valueOf = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.seekbar_bg_color));
            //~ }
            seekBar.setProgressBackgroundTintList(valueOf);
        }
    }

    public static Drawable tintDrawable(Drawable drawable, int i) {
        Drawable wrap = DrawableCompat.wrap(drawable);
        DrawableCompat.setTintList(wrap, ColorStateList.valueOf(i));
        return wrap;
    }
}
