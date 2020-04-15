package org.omnirom.device.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RadioButton;
import androidx.preference.PreferenceViewHolder;
import org.omnirom.device.R;
import com.android.settingslib.TwoTargetPreference;

public class MasterRadioPreference extends TwoTargetPreference {
    private boolean mChecked;
    private boolean mEnable = true;
    private RadioButton mRadioBtn;

    @Override // com.android.settingslib.TwoTargetPreference
    protected int getSecondTargetResId() {
        return R.layout.preference_widget_master_radio_button;
    }

    public MasterRadioPreference(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }

    public MasterRadioPreference(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public MasterRadioPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public MasterRadioPreference(Context context) {
        super(context);
    }

    @Override // com.android.settingslib.TwoTargetPreference, androidx.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        super.onBindViewHolder(preferenceViewHolder);
        View findViewById = preferenceViewHolder.findViewById(16908312);
        if (findViewById != null) {
            findViewById.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (MasterRadioPreference.this.mRadioBtn == null || MasterRadioPreference.this.mRadioBtn.isEnabled()) {
                        MasterRadioPreference masterRadioPreference = MasterRadioPreference.this;
                        masterRadioPreference.setChecked(!masterRadioPreference.mChecked);
                        MasterRadioPreference masterRadioPreference2 = MasterRadioPreference.this;
                        if (!masterRadioPreference2.callChangeListener(Boolean.valueOf(masterRadioPreference2.mChecked))) {
                            MasterRadioPreference masterRadioPreference3 = MasterRadioPreference.this;
                            masterRadioPreference3.setChecked(!masterRadioPreference3.mChecked);
                            return;
                        }
                        MasterRadioPreference masterRadioPreference4 = MasterRadioPreference.this;
                        boolean unused = masterRadioPreference4.persistBoolean(masterRadioPreference4.mChecked);
                    }
                }
            });
        }
        this.mRadioBtn = (RadioButton) preferenceViewHolder.findViewById(R.id.btn_radio);
        RadioButton radioButton = this.mRadioBtn;
        if (radioButton != null) {
            radioButton.setContentDescription(getTitle());
            this.mRadioBtn.setChecked(this.mChecked);
            this.mRadioBtn.setEnabled(this.mEnable);
        }
    }

    @Override // androidx.preference.Preference
    public void setEnabled(boolean z) {
        super.setEnabled(z);
        setRadioBtnEnabled(z);
    }

    public void setChecked(boolean z) {
        this.mChecked = z;
        RadioButton radioButton = this.mRadioBtn;
        if (radioButton != null) {
            radioButton.setChecked(z);
        }
    }

    public void setRadioBtnEnabled(boolean z) {
        this.mEnable = z;
        RadioButton radioButton = this.mRadioBtn;
        if (radioButton != null) {
            radioButton.setEnabled(z);
        }
    }
}
