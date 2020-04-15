/*
* Copyright (C) 2020 The OmniROM Project
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

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.TypedArray;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import android.database.ContentObserver;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Button;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import java.util.Arrays;
import org.omnirom.device.R;

public class GripTapForcePreference extends Preference implements
        SeekBar.OnSeekBarChangeListener {

    private SeekBar mSeekBar;
    private int mOldStrength;
    private int mMinValue;
    private int mMaxValue;
    private int mInterval = 3;

    private String mUnitsLeft = "";
    private String mUnitsRight = "";
    private TextView mStatusText;

    private static final String ANDROIDNS = "http://schemas.android.com/apk/res/android";

    private static final String TAP_FORCE_LEFT = "/proc/driver/grip_tap1_force";
    public static final String GRIP_TAPFORCE_KEY = DeviceSettings.KEY_SETTINGS_PREFIX + GripTapForce.GRIP_TAPFORCE_LEFT_LEFT;
    public static final String DEFAULT_VALUE = "1";

    public GripTapForcePreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setValuesFromXml(context, attrs);
        setLayoutResource(R.layout.preference_seek_bar);
        mMinValue = 1;
        mMaxValue = 30;
        mInterval = 3;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        mOldStrength = Integer.parseInt(getValue(getContext()));
        mSeekBar = (SeekBar) holder.findViewById(R.id.seekbar);
        mSeekBar.setMax(mMaxValue - mMinValue);
        mSeekBar.setProgress(mOldStrength - mMinValue);
        mSeekBar.setOnSeekBarChangeListener(this);

        mStatusText = (TextView) holder.findViewById(R.id.seekBarPrefValue);
        mStatusText.setText(String.valueOf(mOldStrength));
        mStatusText.setMinimumWidth(30);

        TextView unitsRight = (TextView) holder.findViewById(R.id.seekBarPrefUnitsRight);
        unitsRight.setText(mUnitsRight);
        TextView unitsLeft = (TextView) holder.findViewById(R.id.seekBarPrefUnitsLeft);
        unitsLeft.setText(mUnitsLeft);
    }

    private void setValuesFromXml(Context context, AttributeSet attrs) {
        mMaxValue = attrs.getAttributeIntValue(ANDROIDNS, "max", 100);

        final TypedArray attributes = context.obtainStyledAttributes(attrs,
                R.styleable.SeekBarPreference);

        TypedValue minAttr =
                attributes.peekValue(R.styleable.SeekBarPreference_min);
        if (minAttr != null && minAttr.type == TypedValue.TYPE_INT_DEC) {
            mMinValue = minAttr.data;
        }

        TypedValue unitsLeftAttr =
                attributes.peekValue(R.styleable.SeekBarPreference_unitsLeft);
        CharSequence data = null;
        if (unitsLeftAttr != null && unitsLeftAttr.type == TypedValue.TYPE_STRING) {
            if (unitsLeftAttr.resourceId != 0) {
                data = context.getText(unitsLeftAttr.resourceId);
            } else {
                data = unitsLeftAttr.string;
            }
        }
        mUnitsLeft = (data == null) ? "" : data.toString();

        TypedValue unitsRightAttr =
                attributes.peekValue(R.styleable.SeekBarPreference_unitsRight);
        data = null;
        if (unitsRightAttr != null && unitsRightAttr.type == TypedValue.TYPE_STRING) {
            if (unitsRightAttr.resourceId != 0) {
                data = context.getText(unitsRightAttr.resourceId);
            } else {
                data = unitsRightAttr.string;
            }
        }
        mUnitsRight = (data == null) ? "" : data.toString();

        TypedValue intervalAttr =
                attributes.peekValue(R.styleable.SeekBarPreference_interval);
        if (intervalAttr != null && intervalAttr.type == TypedValue.TYPE_INT_DEC) {
            mInterval = intervalAttr.data;
        }

        attributes.recycle();
    }

    public static boolean isSupported() {
        return Utils.fileWritable(TAP_FORCE_LEFT);
    }

    public static String getValue(Context context) {
        String val = Utils.getFileValue(TAP_FORCE_LEFT, DEFAULT_VALUE);
        return val;
    }

    private void setValue(String newValue) {
        Utils.writeValue(TAP_FORCE_LEFT, newValue);
        Settings.System.putString(getContext().getContentResolver(), GRIP_TAPFORCE_KEY, newValue);
    }

    public static void restore(Context context) {
        if (!isSupported()) {
            return;
        }
        String storedValue = Settings.System.getString(context.getContentResolver(), GRIP_TAPFORCE_KEY);
        if (storedValue == null) {
            storedValue = DEFAULT_VALUE;
        }
        Utils.writeValue(TAP_FORCE_LEFT, storedValue);
    }

    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromTouch) {
        int newValue = progress + mMinValue;

        if (newValue > mMaxValue) {
            newValue = mMaxValue;
        } else if (newValue < mMinValue) {
            newValue = mMinValue;
        } else if (mInterval != 1 && newValue % mInterval != 0) {
            newValue = Math.round(((float) newValue) / mInterval) * mInterval;
        }

        // change rejected, revert to the previous value
        if (!callChangeListener(newValue)) {
            seekBar.setProgress(mOldStrength - mMinValue);
            return;
        }

        // change accepted, store it
        mOldStrength = newValue;
        mStatusText.setText(String.valueOf(newValue));
        persistInt(newValue);
        setValue(String.valueOf(newValue));
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        // NA
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        // NA
    }
}
