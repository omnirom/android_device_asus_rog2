package org.omnirom.device.gripsensor;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioButton;
import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.CheckBoxPreference;
import androidx.preference.PreferenceViewHolder;
import org.omnirom.device.R;

public class RadioPreference extends CheckBoxPreference {
    private RadioButton mBtn;
    private boolean mCheck;
    private Context mContext;
    private String mPath;

    public RadioPreference(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mCheck = false;
        setWidgetLayoutResource(R.layout.preference_widget_radio_btn);
        this.mContext = context;
    }

    public RadioPreference(Context context) {
        this(context, null, TypedArrayUtils.getAttr(context, R.attr.preferenceStyle, android.R.attr.preferenceStyle));
    }

    public RadioPreference(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, TypedArrayUtils.getAttr(context, R.attr.preferenceStyle, android.R.attr.preferenceStyle));
    }

    @Override // androidx.preference.CheckBoxPreference, androidx.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        super.onBindViewHolder(preferenceViewHolder);
        this.mBtn = (RadioButton) preferenceViewHolder.findViewById(R.id.widget_radio);
        this.mBtn.setChecked(this.mCheck);
    }

    public void setPath(String str) {
        this.mPath = str;
    }

    public String getPath() {
        return this.mPath;
    }

    @Override // androidx.preference.TwoStatePreference, androidx.preference.Preference
    public void onClick() {
        if (!isChecked()) {
            super.onClick();
        }
    }

    @Override // androidx.preference.TwoStatePreference
    public void setChecked(boolean z) {
        super.setChecked(z);
        this.mCheck = z;
        RadioButton radioButton = this.mBtn;
        if (radioButton != null) {
            radioButton.setChecked(z);
        }
    }
}
