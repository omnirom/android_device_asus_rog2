package org.omnirom.device.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import org.omnirom.device.R;

@TargetApi(23)
public class CustomSettingsButton extends Button {
    private Context mContext;

    public CustomSettingsButton(Context context) {
        this(context, null);
    }

    public CustomSettingsButton(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public CustomSettingsButton(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, R.style.SettingsButtonLight);
    }

    public CustomSettingsButton(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mContext = context;
    }
}
